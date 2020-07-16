package net.ivanhjc.utility.data.model;

import java.util.Date;

/**
 * An object of this type stores the number or counts of something (such as orders, visitors, etc.) on a specific date, and many such objects
 * can be used to comprise the statistics for a histogram.
 *
 * @author Ivan Huang on 2018/6/15 18:04.
 */
public class ObjectCountModel {
    private Date date;
    private Integer count;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
