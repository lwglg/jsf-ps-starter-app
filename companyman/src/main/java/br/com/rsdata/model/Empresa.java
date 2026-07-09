package br.com.rsdata.model;

import br.com.rsdata.validation.CNPJ;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma Empresa.
 */
@Entity
@Table(name = "empresa", uniqueConstraints = {
        @UniqueConstraint(name = "uk_empresa_cnpj", columnNames = "cnpj")
})
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "O nome fantasia é obrigatório.")
    @Size(max = 80, message = "O nome fantasia deve ter no máximo 80 caracteres.")
    @Column(name = "nome_fantasia", nullable = false, length = 80)
    private String nomeFantasia;

    @NotBlank(message = "A razão social é obrigatória.")
    @Size(max = 120, message = "A razão social deve ter no máximo 120 caracteres.")
    @Column(name = "razao_social", nullable = false, length = 120)
    private String razaoSocial;

    @NotBlank(message = "O CNPJ é obrigatório.")
    @Size(max = 18, message = "O CNPJ deve ter no máximo 18 caracteres.")
    @CNPJ(message = "CNPJ inválido: verifique os dígitos informados.")
    @Column(name = "cnpj", nullable = false, length = 18)
    private String cnpj;

    @NotNull(message = "A data de fundação é obrigatória.")
    @PastOrPresent(message = "A data de fundação não pode ser uma data futura.")
    @Temporal(TemporalType.DATE)
    @Column(name = "data_fundacao", nullable = false)
    private Date dataFundacao;

    @NotNull(message = "O ramo de atividade é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ramo_atividade_id", nullable = false, foreignKey = @ForeignKey(name = "fk_empresa_ramo_atividade"))
    private RamoAtividade ramoAtividade;

    @NotNull(message = "O tipo de empresa é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa", nullable = false, length = 20)
    private TipoEmpresa tipoEmpresa;

    @NotNull(message = "O faturamento é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "O faturamento não pode ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "O faturamento deve ter no máximo 8 dígitos inteiros e 2 casas decimais.")
    @Column(name = "faturamento", nullable = false, precision = 10, scale = 2)
    private BigDecimal faturamento;

    public Empresa() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Date getDataFundacao() {
        return dataFundacao;
    }

    public void setDataFundacao(Date dataFundacao) {
        this.dataFundacao = dataFundacao;
    }

    public RamoAtividade getRamoAtividade() {
        return ramoAtividade;
    }

    public void setRamoAtividade(RamoAtividade ramoAtividade) {
        this.ramoAtividade = ramoAtividade;
    }

    public TipoEmpresa getTipoEmpresa() {
        return tipoEmpresa;
    }

    public void setTipoEmpresa(TipoEmpresa tipoEmpresa) {
        this.tipoEmpresa = tipoEmpresa;
    }

    public BigDecimal getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(BigDecimal faturamento) {
        this.faturamento = faturamento;
    }

    /**
     * Igualdade determinada pelo CNPJ (chave de negócio de uma empresa),
     * permitindo detectar duplicidade mesmo antes da persistência.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Empresa)) {
            return false;
        }
        Empresa empresa = (Empresa) o;
        return cnpj != null ? cnpj.equals(empresa.cnpj) : empresa.cnpj == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnpj);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nomeFantasia='" + nomeFantasia + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", tipoEmpresa=" + tipoEmpresa +
                '}';
    }
}
