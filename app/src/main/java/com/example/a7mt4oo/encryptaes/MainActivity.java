package com.example.a7mt4oo.encryptaes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements PermissionUtils.OnPermissionResponse {
    EditText inputText, inputPasword;
    TextView outputText;
    Button encBtn, decBtn,chooseBtn;
    String outputString;
    String AES = "AES";
    CheckBox checkBox;

    PermissionUtils permissionUtils;

    String vFileText="";
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionUtils=new PermissionUtils(this);
        inputText = (EditText) findViewById(R.id.inputText);
        inputPasword = (EditText) findViewById(R.id.password);
        outputText = (TextView) findViewById(R.id.outputText);
        encBtn = (Button) findViewById(R.id.encBtn);
        decBtn = (Button) findViewById(R.id.decBtn);
        chooseBtn = (Button) findViewById(R.id.chooseBtn);
        checkBox = findViewById(R.id.cbDeleteFile);

        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionUtils.requestPermissions(PermissionUtils.REQUEST_EXTERNAL_STORAGE,PermissionUtils.REQUEST_CODE_EXTERNAL_STORAGE);
            }
        });
    }

    public void encClick(View view) {
        try {
            if(inputPasword.getText().toString().trim().length()==0)
                Toast.makeText(getApplicationContext(),"Please enter password",Toast.LENGTH_LONG).show();
            else {
                outputString = encrypt(inputText.getText().toString(), inputPasword.getText().toString());
                outputText.setText(outputString);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }

    private String encrypt(String Data, String password) throws Exception {
        String encryptedvalue;
        SecretKeySpec secretKeySpec = generateKey(password);
        Cipher c = Cipher.getInstance(AES);

        if(inputText.isEnabled()) {

            c.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encval = c.doFinal(Data.getBytes());
            encryptedvalue = Base64.encodeToString(encval, Base64.DEFAULT);
        }else{

            c.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encval = c.doFinal(vFileText.getBytes());
            encryptedvalue = Base64.encodeToString(encval, Base64.DEFAULT);

            String filename=filePath.substring(filePath.lastIndexOf("/")+1);
            String fileNameWithoutExtesion=filename.split(Pattern.quote(".*"))[0];

            Log.e("Tag","filename=>"+filename);

            String vNewFilePath=filePath.replace(filename,"");

            File dir = new File(vNewFilePath);

            File file = new File( dir,fileNameWithoutExtesion+".aes");

            try {
                FileOutputStream os = new FileOutputStream(file);
                os.write(encryptedvalue.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(),"Encryption done successfully.",Toast.LENGTH_SHORT).show();

            if(checkBox.isChecked()) {
                File deleteFile = new File(filePath);
                deleteFile.delete();
            }

            filePath="";
            inputText.setText("");
            inputPasword.setText("");
            outputText.setText("");
            inputText.setEnabled(true);
            checkBox.setChecked(false);

            encryptedvalue="";
            Log.e("Tag","file=>"+file.getAbsolutePath());
        }
        return encryptedvalue;
    }

    private SecretKeySpec generateKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;

    }

    public void decrClick(View view) {
        try {
            if(inputPasword.getText().toString().trim().length()==0)
                Toast.makeText(getApplicationContext(),"Please enter password",Toast.LENGTH_SHORT).show();
            else
                outputString = decrypt(outputString, inputPasword.getText().toString());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"incorrect password",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        outputText.setText(outputString);

    }

    private String decrypt(String outputString, String password)throws Exception {

        String decryptedValue;
        SecretKeySpec secretKeySpec = generateKey(password);
        Cipher c = Cipher.getInstance(AES);

        if(inputText.isEnabled()) {

            c.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedValue = Base64.decode(outputString, Base64.DEFAULT);
            byte[] decValue = c.doFinal(decodedValue);
            decryptedValue = new String(decValue);
        }else{

            c.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedValue = Base64.decode(vFileText, Base64.DEFAULT);
            byte[] decValue = c.doFinal(decodedValue);
            decryptedValue = new String(decValue);

            String filename=filePath.substring(filePath.lastIndexOf("/")+1);
            String fileNameWithoutExtesion=filename.split(Pattern.quote(".aes"))[0];

            Log.e("Tag","filename=>"+filename);

            String vNewFilePath=filePath.replace(filename,"");

            File dir = new File(vNewFilePath);

            File file = new File( dir,fileNameWithoutExtesion+"");

            try {
                FileOutputStream os = new FileOutputStream(file);
                os.write(decryptedValue.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(),"Decryption done successfully.",Toast.LENGTH_SHORT).show();

            filePath="";
            inputText.setText("");
            inputPasword.setText("");
            outputText.setText("");
            inputText.setEnabled(true);
            checkBox.setChecked(false);

            Log.e("Tag","file=>"+file.getAbsolutePath());
        }
        return decryptedValue;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onPermissionGranted(int requestCode) {
//        Intent intent = new Intent();
//        intent.setType("txt/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select file"), 1);

        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                .withFilter(Pattern.compile(".*")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(true) // Set directories filterable (false by default)
                .withHiddenFiles(true) // Show hidden files and folders
                .start();
    }

    @Override
    public void onPermissionDenied(int requestCode) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            Log.e("Tag","filePath=>"+filePath);


            File file = new File(filePath);

//Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();

                vFileText=text.toString();
//                inputText.setText(text.toString());
                inputText.setText(filePath);
                inputText.setEnabled(false);
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.exit) {
            finish();

        }else if (id == R.id.about){

            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}




