package com.money.manager.ex.core;

import net.robotmedia.billing.BillingController;

public class InAppBilling {
	
	public static final BillingController.IConfiguration getConfiguaration(){
		return new BillingController.IConfiguration() {
			public byte[] getObfuscationSalt() {
				return new byte[]{11, 45, -96, -31, -66, 23, 52, 100, 99, -69, 25, -77, -17, 115, 25, 55, 95, -110, -48, -120};
			}

			public String getPublicKey() {
				return Core.getAppBase64();
			}
		};
	}
}
