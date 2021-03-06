package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;

public class Reminder {
	
	private BigInteger Id;   // mongo db id
    private transient long dbId;  // local database id

    private String name;
    private int dayOfWeek;
	private int hour;
	private int minutes;
	private String alarm;
	private boolean on;
    private long created;


    public enum ReminderType {
        PAIN(1), MED(2), GENERIC(3);

        private final int value;

        private ReminderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ReminderType findByValue(int val){
            for(ReminderType r : values()){
                if( r.getValue() == val ){
                    return r;
                }
            }
            return GENERIC;
        }
    }

    private ReminderType reminderType;
	
	public Reminder() {
		super();
        this.hour = -1;
        this.minutes = -1;
        this.on = false;
        this.reminderType = ReminderType.GENERIC;
	}

    public Reminder(String name) {
        super();
        this.name = name;
        this.hour = -1;
        this.minutes = -1;
        this.setOn(false);
        this.reminderType = ReminderType.GENERIC;
    }
	
	public Reminder(String name, int dayOfWeek, int hour, int minutes, String alarm) {
		super();
		this.dayOfWeek = dayOfWeek;
		this.hour = hour;
		this.minutes = minutes;
		this.alarm = alarm;
        this.reminderType = ReminderType.GENERIC;
	}
	
	public BigInteger getId() {
		return Id;
	}
	public void setId(BigInteger id) {
		Id = id;
	}
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	public String getAlarm() {
		return alarm;
	}
	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean isOn) {
		this.on = isOn;
	}

	public ReminderType getReminderType() {
		return reminderType;
	}

	public void setReminderType(ReminderType reminderType) {
		this.reminderType = reminderType;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (created ^ (created >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Reminder))
            return false;
        Reminder other = (Reminder) obj;
        if (created != other.created)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "Id=" + Id +
                ", dbId=" + dbId +
                ", name='" + name + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", hour=" + hour +
                ", minutes=" + minutes +
                ", alarm='" + alarm + '\'' +
                ", on=" + on +
                ", created=" + created +
                ", reminderType=" + reminderType +
                '}';
    }

}
