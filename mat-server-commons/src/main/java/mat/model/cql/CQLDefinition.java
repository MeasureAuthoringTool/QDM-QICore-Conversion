package mat.model.cql;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CQLDefinition implements CQLExpression, IsSerializable {
    private String id;
    private String definitionName;
    private String definitionLogic;
    private String context = "Patient";
    private boolean supplDataElement;
    private boolean popDefinition;
    private String commentString = "";
    private String returnType;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return getDefinitionName();
    }

    @Override
    public void setName(String name) {
        setDefinitionName(name);
    }

    @Override
    public String getLogic() {
        return getDefinitionLogic();
    }

    @Override
    public void setLogic(String logic) {
        setDefinitionLogic(logic);
    }

    public String getDefinitionName() {
        return definitionName.trim();
    }

    public void setDefinitionName(String name) {
        this.definitionName = name.trim();
    }

    public String getDefinitionLogic() {
        return definitionLogic.trim();
    }

    public void setDefinitionLogic(String logic) {
        this.definitionLogic = logic.trim();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isSupplDataElement() {
        return supplDataElement;
    }

    public void setSupplDataElement(boolean supplDataElement) {
        this.supplDataElement = supplDataElement;
    }

    @Override
    public String toString() {
        return this.definitionName;
    }


    public boolean isPopDefinition() {
        return popDefinition;
    }

    public void setPopDefinition(boolean popDefinition) {
        this.popDefinition = popDefinition;
    }

    public String getCommentString() {
        return commentString;
    }

    public void setCommentString(String commentString) {
        this.commentString = commentString;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public static class Comparator implements java.util.Comparator<CQLDefinition>, IsSerializable {
        @Override
        public int compare(CQLDefinition o1,
                           CQLDefinition o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
