package org.tron.common.logsfilter;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.tron.common.crypto.Hash;
import org.tron.common.runtime.TVMTestResult;
import org.tron.common.runtime.TVMTestUtils;
import org.tron.common.runtime.vm.LogInfoTriggerParser;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.core.services.http.JsonFormat;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.SmartContract.ABI;
import org.tron.protos.Protocol.SmartContract.ABI.Builder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventParserTest {
  @Test
  public synchronized void testEventParser() {

    String eventSign = "eventBytesL(address,bytes,bytes32,uint256,string)";

    String abiStr = "[{\"constant\":false,\"inputs\":[{\"name\":\"_address\",\"type\":\"address\"},{\"name\":\"_random\",\"type\":\"bytes\"}],\"name\":\"randomNum\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":true,\"inputs\":[{\"indexed\":true,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"random\",\"type\":\"bytes\"},{\"indexed\":false,\"name\":\"last1\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"t2\",\"type\":\"uint256\"}],\"name\":\"eventAnonymous\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"random\",\"type\":\"bytes\"},{\"indexed\":true,\"name\":\"last1\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"t2\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"str\",\"type\":\"string\"}],\"name\":\"eventBytesL\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"random\",\"type\":\"bytes\"},{\"indexed\":false,\"name\":\"last1\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"t2\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"str\",\"type\":\"string\"}],\"name\":\"eventBytes\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"random\",\"type\":\"bytes\"},{\"indexed\":false,\"name\":\"\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"last1\",\"type\":\"bytes32[]\"},{\"indexed\":false,\"name\":\"t2\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"str\",\"type\":\"string\"}],\"name\":\"eventByteArr\",\"type\":\"event\"}]";

    String dataStr = "0x000000000000000000000000ca35b7d915458ef540ade6068dfe2f44e8fa733c0000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000000020109000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a6162636465666731323300000000000000000000000000000000000000000000";
    ABI abi = TVMTestUtils.jsonStr2ABI(abiStr);

    byte[] data = ByteArray.fromHexString(dataStr);
    List<byte[]> topicList = new LinkedList<>();
    topicList.add(Hash.sha3(eventSign.getBytes()));
    topicList.add(ByteArray.fromHexString("0xb7685f178b1c93df3422f7bfcb61ae2c6f66d0947bb9eb293259c231b986b81b"));

    ABI.Entry entry = null;
    for (ABI.Entry e: abi.getEntrysList()) {
      System.out.println(e.getName());
      if (e.getName().equalsIgnoreCase("eventBytesL")){
        entry = e;
        break;
      }
    }

    Assert.assertEquals(LogInfoTriggerParser.getEntrySignature(entry), eventSign);
    Assert.assertEquals(Hash.sha3(LogInfoTriggerParser.getEntrySignature(entry).getBytes()), topicList.get(0));
    Assert.assertNotNull(entry);
    Map<String, String> dataMap = ContractEventParser.parseEventData(data, topicList, entry);
    Map<String, String> topicMap = ContractEventParser.parseTopics(topicList, entry);

    Assert.assertEquals(dataMap.get("0"), "000000000000000000000000ca35b7d915458ef540ade6068dfe2f44e8fa733c");
    Assert.assertEquals(dataMap.get("addr"), "000000000000000000000000ca35b7d915458ef540ade6068dfe2f44e8fa733c");

    Assert.assertEquals(dataMap.get("1"), "0x109");
    Assert.assertEquals(dataMap.get("random"), "0x109");

    Assert.assertEquals(topicMap.get("2"), "0xB7685F178B1C93DF3422F7BFCB61AE2C6F66D0947BB9EB293259C231B986B81B");
    Assert.assertEquals(topicMap.get("last1"), "0xB7685F178B1C93DF3422F7BFCB61AE2C6F66D0947BB9EB293259C231B986B81B");

    Assert.assertEquals(dataMap.get("3"), "1");
    Assert.assertEquals(dataMap.get("t2"), "1");

    Assert.assertEquals(dataMap.get("4"), "abcdefg123");
    Assert.assertEquals(dataMap.get("str"), "abcdefg123");

  }
}
