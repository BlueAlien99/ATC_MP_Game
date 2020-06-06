package com.atc.client.model;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static com.atc.client.Dimensions.*;
/**
 * Class to detect collisions. Only Rafał Kulus knows how it works.
 */
public class TCAS {

	public final static double warningHorizontal = 72;
	public final static double warningVertical = 1000;
	public final static double criticalHorizontal = 24;
	public final static double criticalVertical = 375;

	//TODO: planes flying at each other
	//TODO: samoloty AI

	public static void calculateSingleCollision(ConcurrentHashMap<UUID, Airplane> airplanes, UUID chosen){
		Airplane iel = airplanes.get(chosen);
		airplanes.forEach((k, v) -> {
			if(!k.equals(chosen)){
				calculate(iel, v);
			}
		});
	}

	/**
	 * Calculates collisions.
	 *
	 * @param airplanes the airplanes
	 */
	public static void calculateCollisions(ConcurrentHashMap<UUID, Airplane> airplanes){
		ArrayList<Airplane> airList = new ArrayList<>(airplanes.values());

		for(int i = 0; i < airList.size(); ++i){
			Airplane iel = airList.get(i);

			if(iel.isCollisionCourse() || iel.isCrashed() || iel.getPosY() < 0 || iel.getPosY() > CANVAS_HEIGHT || iel.getPosX() < 0 || iel.getPosX() > CANVAS_WIDTH){
				continue;
			}

			for(int j = i+1; j < airList.size(); ++j){
				Airplane jel = airList.get(j);

				if(jel.isCrashed()){
					continue;
				}

				calculate(iel, jel);
			}
		}
	}

	private static void calculate(Airplane iel, Airplane jel){
		double verticalSeparation = Math.abs(iel.getAltitude() - jel.getAltitude());
		double horizontalSeparation = Math.sqrt(Math.pow(iel.getPosX() - jel.getPosX(), 2) + Math.pow(iel.getPosY() - jel.getPosY(), 2));

		if(verticalSeparation < criticalVertical && horizontalSeparation < criticalHorizontal){
			iel.setCrashed();
			jel.setCrashed();
			return;
		}

		if(verticalSeparation < warningVertical && horizontalSeparation < warningHorizontal){
			iel.setCollisionCourse();
			jel.setCollisionCourse();
			return;
		}

		if(!DEBUGGING_MODE && verticalSeparation < warningVertical){
			return;
		}

		char ielCase = getTcasCase(iel.getHeading());
		char jelCase = getTcasCase(jel.getHeading());
		boolean collision = false;

		if((ielCase == '8' || ielCase == '2') && (jelCase == '8' || jelCase == '2') && Math.abs(iel.getPosX() - jel.getPosX()) < warningHorizontal){
			if(ielCase != jelCase){
				double coordFar = ielCase == '8' ? iel.getPosY() : jel.getPosY();
				double coordClose = ielCase == '8' ? jel.getPosY() : iel.getPosY();
				if(coordFar - coordClose > 0 && Math.abs(coordFar - coordClose) < iel.getSpeed() + jel.getSpeed()){
					collision = true;
				}
			}
		}

		else if((ielCase == '6' || ielCase == '4') && (jelCase == '6' || jelCase == '4') && Math.abs(iel.getPosY() - jel.getPosY()) < warningHorizontal){
			if(ielCase != jelCase){
				double coordFar = ielCase == '6' ? iel.getPosX() : jel.getPosX();
				double coordClose = ielCase == '6' ? jel.getPosX() : iel.getPosX();
				if(coordFar - coordClose < 0 && Math.abs(coordFar - coordClose) < iel.getSpeed() + jel.getSpeed()){
					collision = true;
				}
			}
		}

		else {
			if (jelCase == '8' || jelCase == '2') {
				char temp = jelCase;
				jelCase = ielCase;
				ielCase = temp;
				Airplane templane = jel;
				jel = iel;
				iel = templane;
			}

			double colx, coly;

			if((ielCase == '8' || ielCase == '2') && (jelCase == '4' || jelCase == '6')){
				colx = iel.getPosX();
				coly = jel.getPosY();
			}
			else if(ielCase == '8' || ielCase == '2'){
				colx = iel.getPosX();
				coly = jel.getColAParam() * colx + jel.getColBParam();
			}
			else{
				colx = (jel.getColBParam() - iel.getColBParam()) / (iel.getColAParam() - jel.getColAParam());
				coly = iel.getColAParam() * colx + iel.getColBParam();
			}

			double disi = Math.sqrt(Math.pow(colx - iel.getPosX(), 2) + Math.pow(coly - iel.getPosY(), 2));
			double disj = Math.sqrt(Math.pow(colx - jel.getPosX(), 2) + Math.pow(coly - jel.getPosY(), 2));

			// check if collision point is in front of a plane
			boolean ieldir = (ielCase == '8' && coly < iel.getPosY()) || (ielCase == '2' && coly > iel.getPosY()) || ((ielCase == '9' || ielCase == '6' || ielCase == '3') && colx > iel.getPosX()) || ((ielCase == '1' || ielCase == '4' || ielCase == '7') && colx < iel.getPosX());
			boolean jeldir = (jelCase == '8' && coly < jel.getPosY()) || (jelCase == '2' && coly > jel.getPosY()) || ((jelCase == '9' || jelCase == '6' || jelCase == '3') && colx > jel.getPosX()) || ((jelCase == '1' || jelCase == '4' || jelCase == '7') && colx < jel.getPosX());

			if(ieldir && jeldir && disi < iel.getSpeed() && disj < jel.getSpeed()){
				collision = true;
			}
		}

		if(collision){
			iel.setCollisionCourse();
			jel.setCollisionCourse();
		}
	}

	/**
	 * Switch funtion used to differentiate cases of collisions.
	 * @param heading
	 * @return
	 */

	private static char getTcasCase(double heading){
		double delta = 2.5;
		if(heading < delta || heading > 360-delta){
			return '8';
		} else if(heading >= delta && heading <= 90-delta){
			return '9';
		} else if(heading > 90-delta && heading < 90+delta){
			return '6';
		} else if(heading >= 90+delta && heading <= 180-delta){
			return '3';
		} else if(heading > 180-delta && heading < 180+delta){
			return '2';
		} else if(heading >= 180+delta && heading <= 270-delta){
			return '1';
		} else if(heading > 270-delta && heading < 270+delta){
			return '4';
		} else if(heading >= 270+delta && heading <= 360-delta){
			return '7';
		}
		return 0;
	}
}
