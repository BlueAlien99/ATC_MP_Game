package com.atc.client.model;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TCAS {

	//TODO: it doesnt take altitude into calculations, but its easy peasy to implement
	//TODO: too sensitive to speed
	//TODO: add collision detection, just calculate distance between two aircrafts
	//TODO: add detection, when two aircrafts are just too close to each other
	//TODO: planes flying at each other

	public static void calculateCollisions(ConcurrentHashMap<String, Airplane> airplanes){
		ArrayList<Airplane> airList = new ArrayList<>(airplanes.values());
		double tooClose = 256;
		double criticallyClose = 64;
		for(int i = 0; i < airList.size(); ++i){
			Airplane iel = airList.get(i);
			char ielCase = getTcasCase(iel.getHeading());
			for(int j = i+1; j < airList.size(); ++j){
				Airplane jel = airList.get(j);
				char jelCase = getTcasCase(jel.getHeading());
				boolean collision = false;

				if((ielCase == '8' || ielCase == '2') && (jelCase == '8' || jelCase == '2') && Math.abs(iel.getPosX() - jel.getPosX()) < tooClose){
					if(ielCase != jelCase){
						double coordFar = ielCase == '8' ? iel.getPosY() : jel.getPosY();
						double coordClose = ielCase == '8' ? jel.getPosY() : iel.getPosY();
						if(coordFar - coordClose > 0 && Math.abs(coordFar - coordClose) < iel.getSpeed() + jel.getSpeed()){
							collision = true;
						}
					}
				}

				else if((ielCase == '6' || ielCase == '4') && (jelCase == '6' || jelCase == '4') && Math.abs(iel.getPosY() - jel.getPosY()) < tooClose){
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
		}
	}

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
