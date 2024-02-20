package se.sundsvall.cimdproxy.cimd;

public interface CIMDMessageListener {

    boolean handleMessage(CIMDMessage message);
}
