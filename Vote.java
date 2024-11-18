

import java.io.Serializable;

public class Vote implements Serializable {
    private String cpf;
    private int option;
    private static final long serialVersionUID = 1L;

    public Vote(String cpf, int option) {
        this.cpf = cpf;
        this.option = option;
    }

    public String getCpf() { return cpf; }
    public int getOption() { return option; }
}