package ist.sec.server;

import static org.junit.Assert.assertTrue;

import java.security.cert.X509Certificate;

import org.junit.Test;

import pt.tecnico.sec.Message;
import pt.tecnico.sec.Notary;
import pt.tecnico.sec.Recorded;
import pt.tecnico.sec.Storage;
import pt.tecnico.sec.signature;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void fakeMessage()
    {
    	Storage store = new Storage();
    	Notary notary = new Notary(1,store,1);
    	
    	signature[] signatures = new signature[] {};
    	Recorded recorded = new Recorded("state",1,1);
    	X509Certificate certificate = null;
    	Message resultMessage = null;
    	Message fakeMessage = new Message("notary1","conteudo",signatures,recorded,certificate);
    	try {
			resultMessage = notary.execute(fakeMessage);
		} catch (Exception e) {
			assertTrue(false);
		}
        assertTrue( resultMessage.getText().equals("") );
    }
}
