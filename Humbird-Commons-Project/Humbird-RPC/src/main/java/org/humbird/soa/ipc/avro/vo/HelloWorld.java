/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.humbird.soa.ipc.avro.vo;

@SuppressWarnings("all")
/** Protocol Greetings */
@org.apache.avro.specific.AvroGenerated
public interface HelloWorld {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"HelloWorld\",\"namespace\":\"org.humbird.soa.db.rpc.avro.vo\",\"doc\":\"Protocol Greetings\",\"types\":[{\"type\":\"record\",\"name\":\"Greeting\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]},{\"type\":\"error\",\"name\":\"Curse\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]}],\"messages\":{\"hello\":{\"doc\":\"Say hello.\",\"request\":[{\"name\":\"greeting\",\"type\":\"Greeting\"}],\"response\":\"Greeting\",\"errors\":[\"Curse\"]}}}");
  /** Say hello. */
  Greeting hello(Greeting greeting) throws org.apache.avro.AvroRemoteException, Curse;

  @SuppressWarnings("all")
  /** Protocol Greetings */
  public interface Callback extends HelloWorld {
    public static final org.apache.avro.Protocol PROTOCOL = HelloWorld.PROTOCOL;
    /** Say hello. */
    void hello(Greeting greeting, org.apache.avro.ipc.Callback<Greeting> callback) throws java.io.IOException;
  }
}