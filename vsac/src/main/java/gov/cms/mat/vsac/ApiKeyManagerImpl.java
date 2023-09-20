package gov.cms.mat.vsac;


public class ApiKeyManagerImpl implements ApiKeyManager {

    private static ApiKeyManager singletonInstance;

    private static final ThreadLocal<String> threadApiKey = new ThreadLocal<>();

    @Override
    public  String getApiKey() {
        try {
        	return threadApiKey.get();
        } finally {
        	threadApiKey.remove();
        }
    }

    @Override
    public  void setApiKey(String apiKey ) {
    	threadApiKey.set(apiKey);
    }

    public static ApiKeyManager getInstance(){
        if (singletonInstance == null){
            singletonInstance = new ApiKeyManagerImpl();
        }

        return singletonInstance;
    }
}
