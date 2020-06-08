package test.com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.server.utils.ChatMsgParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChatMsgParserTest {

	@Test
	void parseNewMsg() {
		Airplane i = new Airplane(null, 0, 0, 5000, 180, 200);
		Airplane j;
		try {
			j = (Airplane)i.clone();
		}catch(CloneNotSupportedException ex){
			return;
		}
		j.setTargetParams(250, 0, 5500);
		String msg = ChatMsgParser.parseNewMsg(i, j);
		Assertions.assertEquals(j.getCallsign() + ", fly heading 360, climb to 5500 feet, speed 250 knots.", msg);

		try {
			i = (Airplane)j.clone();
		}catch(CloneNotSupportedException ex){
			return;
		}
		j.setTargetParams(250, 0, 2000);
		msg = ChatMsgParser.parseNewMsg(i, j);
		Assertions.assertEquals(j.getCallsign() + ", descend to 2000 feet.", msg);
	}
}