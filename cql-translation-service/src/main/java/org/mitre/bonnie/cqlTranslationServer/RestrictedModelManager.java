package org.mitre.bonnie.cqlTranslationServer;

import  org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.elm.r1.VersionedIdentifier;
import java.util.HashSet;

public class RestrictedModelManager extends ModelManager {
   
	private ModelManager coreManager; 
	private boolean disabled = false;
	private HashSet<String> allowedModels = new HashSet<>();
	
	RestrictedModelManager(ModelManager parentManager)
	{
		coreManager = parentManager;
	}
	
	RestrictedModelManager(ModelManager parentManager,String singleModelName)
	{
		coreManager = parentManager;
		allowedModels.add(singleModelName);
	}
	
	public void clearAllowedModels()
	{
		allowedModels.clear();
	}
	
	public void allowModel(String modelName)
	{
		allowedModels.add(modelName);
	}
	
	public boolean isDisabled()
	{
		return disabled;
	}
	
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
	
	public Model resolveModel(String modelName) {
		System.out.println("Resolving Model: "+modelName);
        return resolveModel(modelName, null);
    }

    public Model resolveModel(String modelName, String version) {
    	System.out.println("Resolving Model: "+modelName+" - Version: "+version);
    	
        return resolveModel(new VersionedIdentifier().withId(modelName).withVersion(version));
    }

    public Model resolveModel(VersionedIdentifier modelIdentifier) {

    	System.out.println("Resolving: "+modelIdentifier.toString());
    	
    	if (!allowedModels.contains(modelIdentifier.getId())&&!disabled)
    	{
            throw new IllegalArgumentException(String.format("model %s, use is not premitted in this context",modelIdentifier.getId()));
        }

        return coreManager.resolveModel(modelIdentifier);
    }
}
