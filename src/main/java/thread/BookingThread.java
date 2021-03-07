package thread;

import model.SagendaAccessToken;
import model.SagendaLock;
import model.SagendaSchedule;
import org.apache.commons.lang3.time.DateUtils;
import utility.ConfigUtils;
import utility.SagendaUtils;

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingThread extends Thread {

    public enum STATUS {
        GETTING_TOKEN,
        WAITING_FOR_SCHEDULE,
        LOCKING,
        WAITING_TO_BOOK,
        BOOKING,
        BOOKING_OK,
        BOOKING_FAIL
    }

    private STATUS status = STATUS.GETTING_TOKEN;

    private SagendaLock targetLock;

    private DayOfWeek bookDay;

    private int bookTime;

    private boolean callBooking = false;

    public BookingThread(DayOfWeek bookDay, int bookTime) {
        this.bookDay = bookDay;
        this.bookTime = bookTime;
    }

    public void run() {

        try {

            String token = ConfigUtils.getConfigValue("token");

            SagendaAccessToken accessToken = SagendaUtils.getToken(token);

            if (accessToken == null) {
                return;
            }

            SagendaSchedule targetSchedule = null;
            while (targetSchedule == null) {

                System.out.println("Looking for schedule at " + String.valueOf(bookTime));

                status = STATUS.WAITING_FOR_SCHEDULE;
                String bookingItem = ConfigUtils.getConfigValue("bookingItem");
                
                try {
                	targetSchedule = lookingForTargetShedule(accessToken, bookingItem);
                } catch (Exception e) {
                    System.out.println("Fail to look for the schedule but will keep trying");
                } 

                if (targetSchedule != null) {
                    System.out.println("Found target schedule, detail is following:");
                    System.out.println("From: " + targetSchedule.getFrom().toString());
                    System.out.println("To: " + targetSchedule.getTo().toString());

                } else {
                    // loop every five second
                    if (targetSchedule == null) {
                        System.out.println(String.valueOf(bookTime) + " has no schedule");
                        Thread.sleep(5000);
                    }
                }
            }

            status = STATUS.LOCKING;
            List<SagendaLock> locks = SagendaUtils.lock(accessToken.getAccess_token(), targetSchedule.getIdentifier());

            if (locks != null && locks.size() > 0) {

                targetLock = locks.get(0);
                status = STATUS.WAITING_TO_BOOK;

                synchronized (this) {
                    wait();
                }

//                Calendar cal = Calendar.getInstance();
//                cal.setTime(new Date());

                while ((status != STATUS.BOOKING_OK || status != STATUS.BOOKING_FAIL)) {



                    try {

                        if (status == STATUS.BOOKING &&
                            !callBooking) {

                            callBooking = true;

                            boolean isSuccess = SagendaUtils.book(accessToken.getAccess_token(),
                                    targetLock.getUserIdentifier(),
                                    targetLock.getEventIdentifier(),
                                    ConfigUtils.getConfigValue("email"),
                                    ConfigUtils.getConfigValue("firstName"),
                                    ConfigUtils.getConfigValue("lastName"),
                                    ConfigUtils.getConfigValue("mobilePhone"),
                                    ConfigUtils.getConfigValue("remark"));

                            if (isSuccess) {
                                System.out.println("Book successfully");
                                status = STATUS.BOOKING_OK;
                            } else {
                                System.out.println("Fail");
                                status = STATUS.BOOKING_FAIL;
                            }
                        }


                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

            }

            System.out.println("Thread End");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public STATUS getStatus(){
        return  status;
    }

    public void triggerBooking() {

        status = STATUS.BOOKING;

        synchronized (this) {
            notify();
        }
    }


    private SagendaSchedule lookingForTargetShedule(SagendaAccessToken accessToken, String bookingItemId) throws Exception {


        List<SagendaSchedule> schedules = SagendaUtils.getSchedule(accessToken.getAccess_token(),
                new Date(),
                DateUtils.addDays(new Date(), 10),
                bookingItemId);

        for (SagendaSchedule schedule : schedules) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(schedule.getFrom());
            Integer dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            Integer hour = cal.get(Calendar.HOUR_OF_DAY);

            int insertValue = bookDay.getValue() + 1;

            //Wed and 8
            if (dayOfWeek == insertValue && hour == bookTime) {
                return schedule;
            }
        }

        return null;
    }
}
