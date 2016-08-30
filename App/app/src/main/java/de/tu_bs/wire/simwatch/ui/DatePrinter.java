package de.tu_bs.wire.simwatch.ui;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by mw on 29.08.16.
 */
public class DatePrinter {

    public String print(Date date) {
        return print(date, Format.SHORT);
    }

    public String print(Date date, Format format) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        switch (format) {
            default:
            case SHORT:
                return dateFormat.format(date);
            case MEDIUM:
                return String.format("%s %s", dateFormat.format(date), timeFormat.format(date));
        }
    }

    public enum Format {SHORT, MEDIUM}

}
