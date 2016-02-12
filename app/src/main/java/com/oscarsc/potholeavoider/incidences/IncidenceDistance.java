package com.oscarsc.potholeavoider.incidences;

import java.io.Serializable;

public class IncidenceDistance implements Serializable,Comparable<IncidenceDistance> {
	private static final long serialVersionUID = 1L;
	private Incidence incidence;
	private double distance;
	private int position;
	public IncidenceDistance(Incidence incidence, double distance,int position) {
		this.incidence = incidence;
		this.distance = distance;
		this.position=position;
	}

	public String speakIncidence() {
		return incidence.voiceSelectedLanguage((int) (distance * 1000));
	}

	@Override
	public int compareTo(IncidenceDistance another) {
		if (distance == another.getDistance())
			return 0;
		if (distance > another.getDistance())
			return 1;
		return -1;
	}
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getDistance() {
		return (int)(distance*1000);
	}

	public String toString() {
		return incidence.toString() + "\n" + "Distance: " + distance;
	}

	public Incidence getIncidence() {
		return incidence;
	}
    @Override
    public boolean equals(Object o){
        if(o instanceof IncidenceDistance) {
            IncidenceDistance id = (IncidenceDistance) o;
            if (incidence.getId() == id.getIncidence().getId())
                return true;
        }
        return false;
    }
}
