import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Conta implements Comparable<Conta> {
    protected int idConta;
    protected String nomePessoa;
    protected String email;
    protected String nomeUsuario;
    protected String senha;
    protected String cpf;
    protected String cidade;
    protected int transferenciasRealizadas;
    protected float saldoConta;

    public int getIdConta() {
        return idConta;
    }
    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }
    public String getNomePessoa() {
        return nomePessoa;
    }
    public void setNomePessoa(String nomePessoa) {
        this.nomePessoa = nomePessoa;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getNomeUsuario() {
        return nomeUsuario;
    }
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    public String getCidade() {
        return cidade;
    }
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    public int getTransferenciasRealizadas() {
        return transferenciasRealizadas;
    }
    public void setTransferenciasRealizadas(int transferenciasRealizadas) {
        this.transferenciasRealizadas = transferenciasRealizadas;
    }
    public float getSaldoConta() {
        return saldoConta;
    }
    public void setSaldoConta(float saldoConta) {
        this.saldoConta = saldoConta;
    }
    
    public Conta(){};   
    public Conta(int idConta, String nomePessoa, String string, String nomeUsuario, String senha,
            String cpf, String cidade, int transferenciasRealizadas, float saldoConta) {
        this.idConta = idConta;
        this.nomePessoa = nomePessoa;
        this.email = email;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.cpf = cpf;
        this.cidade = cidade;
        this.transferenciasRealizadas = transferenciasRealizadas;
        this.saldoConta = saldoConta;
    }

    public byte[] converteContaEmByte()throws IOException{
        
        ByteArrayOutputStream vetorByte = new ByteArrayOutputStream();
        DataOutputStream buffer = new DataOutputStream(vetorByte);
        buffer.writeInt(idConta);
        buffer.writeUTF(nomePessoa);
        buffer.writeUTF(email);
        buffer.writeUTF(nomeUsuario);
        buffer.writeUTF(senha);
        buffer.writeUTF(cpf);
        buffer.writeUTF(cidade);
        buffer.writeInt(transferenciasRealizadas);
        buffer.writeFloat(saldoConta);
        return vetorByte.toByteArray();
    }

    public void decodificaByteArray(byte[] vetorByte)throws IOException{
        
        ByteArrayInputStream bufferParaLeitura = new ByteArrayInputStream(vetorByte);
        DataInputStream leitura = new DataInputStream(bufferParaLeitura);
        idConta = leitura.readInt();
        nomePessoa = leitura.readUTF();
        email = leitura.readUTF();
        nomeUsuario = leitura.readUTF();
        senha = leitura.readUTF();
        cpf = leitura.readUTF();
        cidade = leitura.readUTF();
        transferenciasRealizadas = leitura.readInt();
        saldoConta = leitura.readFloat();
        
    }

    @Override
    public String toString() {
        return "Conta [idConta=" + idConta + ", nomePessoa=" + nomePessoa + ", email=" + email
                + ", nomeUsuario=" + nomeUsuario + ", senha=" + senha + ", cpf=" + cpf + ", cidade="
                + cidade + ", transferenciasRealizadas=" + transferenciasRealizadas
                + ", saldoConta=" + saldoConta + "]";
    }
    @Override
    public int compareTo(Conta o) { 
        return this.idConta - o.idConta;
    }

    public byte[] toByteArray() throws IOException{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        
        dos.writeInt(idConta);
        dos.writeInt(nomePessoa.length());
        dos.writeUTF(nomePessoa);
        dos.writeInt(email.length());
        dos.writeInt(nomeUsuario.length());
        dos.writeUTF(nomeUsuario);
        dos.writeInt(senha.length());
        dos.writeUTF(senha);
        dos.writeUTF(cpf);
        dos.writeInt(cidade.length());
        dos.writeUTF(cidade);
        dos.writeInt(transferenciasRealizadas);
        dos.writeFloat(saldoConta);
        
        
        return baos.toByteArray();
    }

    
}
