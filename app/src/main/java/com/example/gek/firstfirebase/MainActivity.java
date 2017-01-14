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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * FireBase работает следующим образом:
 * 1) Получив ссылку на БД мы можем вносить данные или что-то менять
 * 2) Для получения данных в программу мы должны описать лисенеры на определенные события,
 *      конкретных дочерних элементов БД. Именно лисенеры и возвратят нам данные, которые
 *      будем использовать для отображения в программе.
 * 3) БД работает и офлайн при включенном setPersistenceEnabled. Все изменения в БД будут хранится
 *      в программе пока не появится инет. После этого все синхронизируется
 * 4) Для многопользовательского доступа к изменениям в БД предусмотренны транзакции
 * */

public class MainActivity extends AppCompatActivity {

    private final static int SIGN_IN_REQUEST_CODE = 99;
    LinearLayout activity_main;
    Button btnAdd, btnRemove, btnSignOut, btnAddUser;
    EditText etName, etPhone, etUser;
    TextView tvInfo;

    private final static String TAG = "DEBUG";

    // в этом ключе хранятся записи с карточками
    private final static String CHILD_LIST = "list";

    // тут хранятся юзеры
    private final static String CHILD_USERS = "users";
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
        etUser = (EditText)findViewById(R.id.etUser);

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

        btnAddUser = (Button)findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });

        findViewById(R.id.btnImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ctx, ImageActivity.class));
            }
        });

        /** Если нет авторизации то выводим окно для нее */
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);
        } else {

            // Получаем ссылку на базу
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Описываем слушатель, который возвращает в программу весь список данных,
            // которые находятся в child(CHILD_LIST)
            // В итоге при любом изменении вся база перезаливается с БД в программу
            ValueEventListener contactCardListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long num = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Load all list ContactCards: total Children objects:" + num);
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

            // устанавливаем слушатель на изменения в нашей базе в разделе контактов
            mDatabase.child(CHILD_LIST).addValueEventListener(contactCardListener);

            // слушатель на изменения полей потомка CHILD_USERS
            mDatabase.child(CHILD_USERS).addChildEventListener(childUserEventListener);
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
            mDatabase.child(CHILD_LIST).child(etName.getText().toString()).removeValue();
            etName.setText("");
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
            mDatabase.child(CHILD_LIST).child(name).setValue(new ContactCard(name, phone, autor));
            etName.setText("");
            etPhone.setText("");
        } else {
            Toast.makeText(ctx, "Fields is empty!", Toast.LENGTH_SHORT).show();
        }
    }


    /** Слушает изменения внутри потомка и возвращает только измененное значение и его ключ
     * Мы мониторим список в потомке CHILD_USERS. Каждое событие отрабатывается отдельно, что
     * очень экономично.
     * Событие срабатывает при любых изменениях данных: как с программы так и с коносоли */
    ChildEventListener childUserEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildAdded: " + dataSnapshot.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChilChanged: " +  dataSnapshot.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved: " +  dataSnapshot.toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildMoved: " +  dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    /** Добавляем юзеров в БД:
     * Сначала создаем укникальный ключ  и потом уже имея это значение помещаем туда нашего юзера */
    private void addUser(){
        String user = etUser.getText().toString();
        if (! user.isEmpty()) {
            String key = mDatabase.child(CHILD_USERS).push().getKey();
            mDatabase.child(CHILD_USERS).child(key).setValue(user);
            Toast.makeText(ctx, "New user " + user + " saved.", Toast.LENGTH_SHORT).show();
            etUser.setText("");
        } else {
            Toast.makeText(ctx, "Field user is empty!", Toast.LENGTH_SHORT).show();
        }
    }


}
