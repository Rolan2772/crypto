package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.ApiSecretProperties;
import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SignatureGeneratorTest {

    private static final String SIGNATURE = "8662e56994002c90b60e98e1e8e5" +
            "5ec1acbcf049a330ab0b01080f52f6b16c4bd83eda3b7d" +
            "d50918a266ed3322e0191db4dcd288ecf3eac8d8692d73" +
            "e28824f5";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Spy
    private PoloniexProperties properties;

    @Spy
    @InjectMocks
    private SignatureGenerator generator = new SignatureGenerator();

    @Before
    public void before() {
        ApiSecretProperties secret = new ApiSecretProperties();
        secret.setSignature(SIGNATURE);
        properties.setSecret(secret);
    }

    @Test
    public void stability() {
        String message = "nonce=" + System.currentTimeMillis();

        String signature = generator.sign(message);
        assertEquals(signature, generator.sign(message));
    }

    @Test
    public void wrongAlgorithm() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Failed to instantiate MAC for WRONG-SHA");
        new SignatureGenerator("WRONG-SHA");
    }
}
