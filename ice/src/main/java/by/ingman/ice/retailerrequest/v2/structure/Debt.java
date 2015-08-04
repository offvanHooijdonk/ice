package by.ingman.ice.retailerrequest.v2.structure;

public class Debt {
    //code_k
    private String contrAgentCode;
    // debt
    private String debt;
    // overdue
    private String overdueDebt;
    //rating
    private String text;

    public Debt(String contrAgentCode, String text, String debt, String overdueDebt) {
        this.contrAgentCode = contrAgentCode;
        this.text = text;
        this.debt = debt;
        this.overdueDebt = overdueDebt;
    }

    public String getContrAgentCode() {
        return contrAgentCode;
    }

    public String getDebt() {
        return debt;
    }

    public String getOverdueDebt() {
        return overdueDebt;
    }

    public String getText() {
        return text;
    }
}
