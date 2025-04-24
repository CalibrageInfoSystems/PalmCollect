package com.cis.palm360collection.weighbridge;


import com.cis.palm360collection.weighbridge.driver.CdcAcmSerialDriver;
import com.cis.palm360collection.weighbridge.driver.ProbeTable;
import com.cis.palm360collection.weighbridge.driver.UsbSerialProber;

public class CustomProber {

    public static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver.class); // e.g. Digispark CDC
        return new UsbSerialProber(customTable);
    }

}

