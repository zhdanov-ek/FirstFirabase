package com.example.gek.firstfirebase;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private final static int SIGN_IN_REQUEST_CODE = 99;
    LinearLayout activity_main;
    Button btnAdd, btnRemove, btnSignOut;
    EditText etName, etPhone;
    TextView tvInfo;

    private final static String TAG = "FirstFirebase";
    Context ctx;

    // Через эту сущность мы записываем данные в базу и обновляем списки на устройстве
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;
        activity_main = (LinearLayout)findViewById(R.id.activity_main);
        etName = (EditText)findViewById(R.id.etName);
        etPhone = (EditText)findViewById(R.id.etPhone);
        tvInfo = (TextView) findViewById(R.id.tvInfo);

        btnSignOut = (Button)findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        btnAdd = (Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });

        btnRemove = (Button)findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeContact();
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

            // Описываем слушатель и его действия
            ValueEventListener contactCardListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long num = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "onDataChange: total Children objects:" + num);
                    tvInfo.setText("Total count of cards: " + num + "\n");
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        ContactCard contactCard = child.getValue(ContactCard.class);
                        tvInfo.append(contactCard.getName() + " - " + contactCard.getPhone() + "\n");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };

            // устанавливаем слушатель на изменения в нешей базе в нужном разделе
            mDatabase.child("list").addValueEventListener(contactCardListener);
        }
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
            } else {
                Snackbar.make(activity_main, "Вход не выполнен", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /** Удаление записи из БД:
     * Через ссылку на БД указываем на конкретный ключ ниже которого все будет удалено.
     * В нашем случае это поле соответствует имени потому как у нас оно уникально.
     * */
    private void removeContact(){
        if (etName.getText().length() > 0) {
            mDatabase.child("list").child(etName.getText().toString()).removeValue();
        } else {
            Toast.makeText(ctx, "Name is empty!", Toast.LENGTH_SHORT).show();
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

    /** Добавлеие записи в БД:
     * К введенным значениям с активити добавляем инфу о пользователе, который авторизирован
     * Потом собственно записываем новую запись в БД */
    private void addContact(){
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
        //                  уже есть то все данные внутри ключа перезапишутся
        if ((name.length() != 0) && (phone.length() != 0)) {
            mDatabase.child("list").child(name).setValue(new ContactCard(name, phone, autor));
            etName.setText("");
            etPhone.setText("");
        } else {
            Toast.makeText(ctx, "Fields is empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
