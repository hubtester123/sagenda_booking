import thread.BookingThread;
import utility.ConfigUtils;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class main {

    public static void main(String[] args) throws Exception {

        HashMap<String, BookingThread> threadHashMap = new HashMap<>();

        String bookDay = ConfigUtils.getConfigValue("targetBookDay");
        String bookTime = ConfigUtils.getConfigValue("targetBookTime");

        List<String> bookingTimeList = Arrays.asList(bookTime.split(","));

        for (String bookingTime: bookingTimeList) {

            BookingThread thread = new BookingThread(DayOfWeek.valueOf(bookDay), Integer.valueOf(bookingTime));
            threadHashMap.put(bookingTime, thread);
            thread.start();
        }

        // Do booking at 5
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        while (!isAllThreadEnd(threadHashMap)) {

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour < 17) {
                cal.setTime(new Date());
            } else {
                if (isAllThreadReadyToBook(threadHashMap)) {
                    System.out.println("Fire Booking");
                    for (String key: threadHashMap.keySet()) {
                        BookingThread thread = threadHashMap.get(key);
                        thread.triggerBooking();
                    }
                }
            }

        }

        System.out.println("Booking done");

    }

    private static boolean isAllThreadEnd(HashMap<String, BookingThread> threadHashMap) {

        boolean isAllThreadEnd = true;
        for (String key: threadHashMap.keySet()) {
            BookingThread thread = threadHashMap.get(key);
            if (thread.getStatus() != BookingThread.STATUS.BOOKING_OK &&
                thread.getStatus() != BookingThread.STATUS.BOOKING_FAIL) isAllThreadEnd = false;
        }
        return isAllThreadEnd;
    }

    private static boolean isAllThreadReadyToBook(HashMap<String, BookingThread> threadHashMap) {

        boolean isAllThreadReady = true;
        for (String key: threadHashMap.keySet()) {
            BookingThread thread = threadHashMap.get(key);
            if (thread.getStatus() != BookingThread.STATUS.WAITING_TO_BOOK) isAllThreadReady = false;
        }
        return isAllThreadReady;
    }


}
