package com.example.gek.firstfirebase;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private final static int SIGN_IN_REQUEST_CODE = 99;
    LinearLayout activity_main;
    Button btnAdd, btnReload, btnSignOut;
    EditText etName, etPhone;

    // Через эту сущность мы записываем данные в базу и обновляем списки на устройстве
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        activity_main = (LinearLayout)findViewById(R.id.activity_main);
        etName = (EditText)findViewById(R.id.etName);
        etPhone = (EditText)findViewById(R.id.etPhone);

        findViewById(R.id.btnSignOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        btnAdd = (Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // формируем объект
                String name = etName.getText().toString();
                String phone = etPhone.getText().toString();
                String autor;
                if (FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                    autor = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                } else {
                    autor = "zorro";
                }

                // Записываем объект в базу в папку list (если ее нет то она будет создана)
                // .push() - создаем дочерний уникальный ключ и уже внутри него помещаются данные
                // .child(name) - формирует ключ внутри которого создается сам объект. Если такой ключ
                //                  уже есть то он перезапишется
                mDatabase.child("list").child(name).setValue(new ContactCard(name, phone, autor));

                etName.setText("");
                etPhone.setText("");
            }
        });


        /** Если нет авторизации то выводим окно для нее */
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);
        } else {

            // Получаем ссылку на базу
            mDatabase = FirebaseDatabase.getInstance().getReference();
            reloadInfo();
        }
    }

    private void reloadInfo(){

    }

    /** Получаем результат работы с окном авторизации */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main, "Вход выполнен", Snackbar.LENGTH_SHORT).show();
                reloadInfo();
            } else {
                Snackbar.make(activity_main, "Вход не выполнен", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /** Выход из учетной записи: обнуляем наш инстанс и выводим активити FireBase для авторизации */
    private void signOut(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);
        }
    }
}
