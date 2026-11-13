package com.induztek.usuarios.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity 
@Table(name="tokens_acceso", indexes=@Index(name="idx_token_hash", columnList="token_hash"))
public class TokenAcceso {
 @Id 
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;
 @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
 @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="usuario_id", nullable=false) private Usuario usuario;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TipoToken tipo;
 @Column(nullable=false) private LocalDateTime expiraEn;
 @Column(nullable=false) private boolean usado=false;
 public Long getId(){return id;} public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;}
 public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;}
 public TipoToken getTipo(){return tipo;} public void setTipo(TipoToken v){tipo=v;}
 public LocalDateTime getExpiraEn(){return expiraEn;} public void setExpiraEn(LocalDateTime v){expiraEn=v;}
 public boolean isUsado(){return usado;} public void setUsado(boolean v){usado=v;}
 public enum TipoToken { INVITACION, RECUPERACION }
}
