package com.example.ice.helpers;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 12.05.13
 * Time: 11:11
 * To change this template use File | Settings | File Templates.
 */
public class StaticFileNames {
    //public static final String DIR_SD = "My SugarSync Folders/My SugarSync";
    public static final String REMOTE_ADDRESS_URL = "file://///80.249.87.247/AndroidExchange";
    public static final String REMOTE_ADDRESS_UPDATE_FILES_DIR = "Pub";

    public static final String CLIENTS_CSV_SD = "clients.csv";
    public static final String RESTS_CSV_SD = "rests.csv";
    public static final String DEBTS_CSV_SD = "debts.csv";
    public static final String FILENAME_WRITE_SD = "out.txt";


    public static String[] getFilenamesArray() {
        return new String[] {CLIENTS_CSV_SD, RESTS_CSV_SD, DEBTS_CSV_SD};
    }
}
