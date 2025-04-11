package tftcalculator.tftcalculator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TftConstants {
    
    @Value("${tft.set.prefix}")
    private String tftPrefix;
    
    public String getTftPrefix() {
        return tftPrefix;
    }
}