package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.db.DatabaseHelper;;

/**
 * Created by Rangika on 11/20/2016.
 */

public class PersistentAccountDAO implements AccountDAO {

    private Context context;

    public PersistentAccountDAO(Context context) {
        this.context = context;
    }

    @Override
    public List<String> getAccountNumbersList() {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        if( handler == null){
            System.out.print("Damn");
        }
        SQLiteDatabase db = handler.getReadableDatabase();

        String query = "SELECT "+ handler.accountNoNo+" FROM " + handler.accountTable+" ORDER BY " + handler.accountNoNo + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<String> resultSet = new ArrayList<>();

        while (cursor.moveToNext())
        {
            resultSet.add(cursor.getString(cursor.getColumnIndex(handler.accountNoNo)));
        }

        cursor.close();


        return resultSet;

    }

    @Override
    public List<Account> getAccountsList() {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = handler.getReadableDatabase();

        String query = "SELECT * FROM " + handler.accountTable+" ORDER BY "+handler.accountNoNo+" ASC";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<Account> resultSet = new ArrayList<>();

        while (cursor.moveToNext())
        {
            Account account = new Account(cursor.getString(cursor.getColumnIndex(handler.accountNoNo)),
                    cursor.getString(cursor.getColumnIndex(handler.bankName)),
                    cursor.getString(cursor.getColumnIndex(handler.accountHolderName)),
                    cursor.getDouble(cursor.getColumnIndex(handler.balance)));

            resultSet.add(account);
        }

        cursor.close();

        return resultSet;

    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = handler.getReadableDatabase();

        String query = "SELECT * FROM " + handler.accountTable + " WHERE " + handler.accountNoNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query, null);

        Account account = null;

        if (cursor.moveToFirst()) {
            account = new Account(cursor.getString(cursor.getColumnIndex(handler.accountNoNo)),
                    cursor.getString(cursor.getColumnIndex(handler.bankName)),
                    cursor.getString(cursor.getColumnIndex(handler.accountHolderName)),
                    cursor.getDouble(cursor.getColumnIndex(handler.balance)));
        }
        else {
            throw new InvalidAccountException("You have selected an invalid account number...!");
        }

        cursor.close();
        return account;
    }

    @Override
    public void addAccount(Account account) {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = handler.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(handler.accountNo, account.getAccountNo());
        values.put(handler.bankName, account.getBankName());
        values.put(handler.accountHolderName, account.getAccountHolderName());
        values.put(handler.balance, account.getBalance());

        db.insert(handler.accountTable, null, values);

    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = handler.getWritableDatabase();

        String query = "SELECT * FROM " + handler.accountTable + " WHERE " + handler.accountNoNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query, null);

        Account account = null;

        if (cursor.moveToFirst()) {
            account = new Account(cursor.getString(cursor.getColumnIndex(handler.accountNoNo)),
                    cursor.getString(cursor.getColumnIndex(handler.bankName)),
                    cursor.getString(cursor.getColumnIndex(handler.accountHolderName)),
                    cursor.getFloat(cursor.getColumnIndex(handler.balance)));
            db.delete(handler.accountTable, handler.accountNo + " = ?", new String[] { accountNo });
            cursor.close();

        }
        else {
            throw new InvalidAccountException("No such account found...!");
        }

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {

        DatabaseHelper handler = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = handler.getWritableDatabase();

        ContentValues values = new ContentValues();

        Account account = getAccount(accountNo);

        if (account!=null) {

            double new_amount=0;


            if (expenseType.equals(ExpenseType.EXPENSE)) {
                new_amount = account.getBalance() - amount;
            }
            else if (expenseType.equals(ExpenseType.INCOME)) {
                new_amount = account.getBalance() + amount;
            }


            String strSQL = "UPDATE "+handler.accountTable+" SET "+handler.balance+" = "+new_amount+" WHERE "+handler.accountNoNo+" = '"+ accountNo+"'";

            db.execSQL(strSQL);

        }

        else {
            throw new InvalidAccountException("No such account found...!");
        }

    }
}
