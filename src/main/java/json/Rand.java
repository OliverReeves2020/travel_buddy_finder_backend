package json;

public class Rand {
    private String jsonrpc;
    private Result result;
    private long id;

    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String value) { this.jsonrpc = value; }

    public Result getResult() { return result; }
    public void setResult(Result value) { this.result = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }
}