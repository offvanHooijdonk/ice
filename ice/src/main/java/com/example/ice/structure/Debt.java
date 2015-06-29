package com.example.ice.structure;

public class Debt {
    private String contrAgentCode;
    private String debt;
    private String overdueDebt;
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
