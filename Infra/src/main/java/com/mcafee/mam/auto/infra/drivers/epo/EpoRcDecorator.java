package com.mcafee.mam.auto.infra.drivers.epo;

public abstract class EpoRcDecorator
{
	protected EPOClient epoClient;

	public EpoRcDecorator(EPOClient newEpoClient)
	{
		epoClient = newEpoClient;
	}
}
