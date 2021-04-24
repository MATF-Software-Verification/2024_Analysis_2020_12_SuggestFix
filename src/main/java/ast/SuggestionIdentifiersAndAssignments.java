package ast;

public class SuggestionIdentifiersAndAssignments extends SuggestionNode {

    private String initialization;

    private String assignment;

    public SuggestionIdentifiersAndAssignments() {
        super();
    }

    public String getInitialization() {
        return initialization;
    }

    public void setInitialization(String initialization) {
        this.initialization = initialization;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }
}
