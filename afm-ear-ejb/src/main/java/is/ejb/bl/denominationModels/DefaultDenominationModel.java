package is.ejb.bl.denominationModels;

import java.util.ArrayList;

public class DefaultDenominationModel {
	ArrayList<DenominationModelRow> listRows = new ArrayList<DenominationModelRow>();

	public static void main(String[] args) {
		DefaultDenominationModel d = new DefaultDenominationModel();
		d.calculateAirtimeReward(4);
	}
	
	public ArrayList<DenominationModelRow> getListRows() {
		return listRows;
	}

	public void setListRows(ArrayList<DenominationModelRow> listRows) {
		this.listRows = listRows;
	}
	
	public DefaultDenominationModel() {
		System.out.println("generating default denomination model content...");
		listRows = new ArrayList<DenominationModelRow>();

		DenominationModelRow r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.06);
		r.setTargetOfferPayoffValue(5.5);
		r.setRevenueSpit(0.5);
		r.setAirtimePayoff(5);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.07);
		r.setTargetOfferPayoffValue(6.42);
		r.setRevenueSpit(1.42);
		r.setAirtimePayoff(5);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.08);
		r.setTargetOfferPayoffValue(7.34);
		r.setRevenueSpit(2.34);
		r.setAirtimePayoff(5);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.09);
		r.setTargetOfferPayoffValue(8.26);
		r.setRevenueSpit(3.26);
		r.setAirtimePayoff(5);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.1);
		r.setTargetOfferPayoffValue(9.17);
		r.setRevenueSpit(4.17);
		r.setAirtimePayoff(5);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.2);
		r.setTargetOfferPayoffValue(18.35);
		r.setRevenueSpit(8.35);
		r.setAirtimePayoff(10);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.3);
		r.setTargetOfferPayoffValue(27.52);
		r.setRevenueSpit(2.52);
		r.setAirtimePayoff(25);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.35);
		r.setTargetOfferPayoffValue(32.11);
		r.setRevenueSpit(2.11);
		r.setAirtimePayoff(30);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.4);
		r.setTargetOfferPayoffValue(36.70);
		r.setRevenueSpit(1.7);
		r.setAirtimePayoff(35);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.45);
		r.setTargetOfferPayoffValue(41.29);
		r.setRevenueSpit(1.29);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.50);
		r.setTargetOfferPayoffValue(45.87);
		r.setRevenueSpit(0.87);
		r.setAirtimePayoff(45);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.55);
		r.setRevenueSpit(0.46);
		r.setTargetOfferPayoffValue(50.46);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.60);
		r.setTargetOfferPayoffValue(55.05);
		r.setRevenueSpit(0.05);
		r.setAirtimePayoff(55);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.65);
		r.setRevenueSpit(4.63);
		r.setTargetOfferPayoffValue(59.63);
		r.setAirtimePayoff(55);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.70);
		r.setTargetOfferPayoffValue(64.22);
		r.setRevenueSpit(4.22);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.75);
		r.setTargetOfferPayoffValue(68.81);
		r.setRevenueSpit(3.81);
		r.setAirtimePayoff(65);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.8);
		r.setTargetOfferPayoffValue(73.40);
		r.setRevenueSpit(3.4);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.85);
		r.setTargetOfferPayoffValue(77.98);
		r.setRevenueSpit(2.98);
		r.setAirtimePayoff(75);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.90);
		r.setTargetOfferPayoffValue(82.57);
		r.setRevenueSpit(2.57);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(0.95);
		r.setTargetOfferPayoffValue(87.16);
		r.setRevenueSpit(2.16);
		r.setAirtimePayoff(85);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.0);
		r.setTargetOfferPayoffValue(91.75);
		r.setRevenueSpit(1.75);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.05);
		r.setTargetOfferPayoffValue(96.33);
		r.setRevenueSpit(1.33);
		r.setAirtimePayoff(95);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.1);
		r.setTargetOfferPayoffValue(100.92);
		r.setRevenueSpit(0.92);
		r.setAirtimePayoff(100);
		listRows.add(r);

		
		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.15);
		r.setTargetOfferPayoffValue(105.51);
		r.setRevenueSpit(0.51);
		r.setAirtimePayoff(105);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.20);
		r.setTargetOfferPayoffValue(110.10);
		r.setRevenueSpit(36.70);
		r.setAirtimePayoff(30);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.25);
		r.setTargetOfferPayoffValue(114.68);
		r.setRevenueSpit(38.23);
		r.setAirtimePayoff(30);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.30);
		r.setTargetOfferPayoffValue(119.27);
		r.setRevenueSpit(39.76);
		r.setAirtimePayoff(30);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.35);
		r.setTargetOfferPayoffValue(123.86);
		r.setRevenueSpit(41.29);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.40);
		r.setTargetOfferPayoffValue(128.44);
		r.setRevenueSpit(42.81);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.45);
		r.setTargetOfferPayoffValue(133.03);
		r.setRevenueSpit(44.34);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.5);
		r.setTargetOfferPayoffValue(137.62);
		r.setRevenueSpit(45.87);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.55);
		r.setTargetOfferPayoffValue(142.21);
		r.setRevenueSpit(47.40);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.6);
		r.setTargetOfferPayoffValue(146.79);
		r.setRevenueSpit(48.93);
		r.setAirtimePayoff(40);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.65);
		r.setTargetOfferPayoffValue(151.38);
		r.setRevenueSpit(50.46);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.7);
		r.setTargetOfferPayoffValue(155.97);
		r.setRevenueSpit(51.99);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.75);
		r.setTargetOfferPayoffValue(160.56);
		r.setRevenueSpit(53.52);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.8);
		r.setTargetOfferPayoffValue(165.14);
		r.setRevenueSpit(55.05);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.85);
		r.setTargetOfferPayoffValue(169.73);
		r.setRevenueSpit(56.58);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.9);
		r.setTargetOfferPayoffValue(174.32);
		r.setRevenueSpit(58.11);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(1.95);
		r.setTargetOfferPayoffValue(178.90);
		r.setRevenueSpit(59.63);
		r.setAirtimePayoff(50);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.0);
		r.setTargetOfferPayoffValue(183.49);
		r.setRevenueSpit(61.16);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.05);
		r.setTargetOfferPayoffValue(188.08);
		r.setRevenueSpit(62.69);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.1);
		r.setTargetOfferPayoffValue(192.67);
		r.setRevenueSpit(64.22);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.15);
		r.setTargetOfferPayoffValue(197.25);
		r.setRevenueSpit(65.75);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.20);
		r.setTargetOfferPayoffValue(201.84);
		r.setRevenueSpit(67.28);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.25);
		r.setTargetOfferPayoffValue(206.43);
		r.setRevenueSpit(68.81);
		r.setAirtimePayoff(60);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.3);
		r.setTargetOfferPayoffValue(211.02);
		r.setRevenueSpit(70.34);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.35);
		r.setTargetOfferPayoffValue(215.60);
		r.setRevenueSpit(71.87);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.40);
		r.setTargetOfferPayoffValue(220.19);
		r.setRevenueSpit(73.40);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.45);
		r.setTargetOfferPayoffValue(224.78);
		r.setRevenueSpit(74.93);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.5);
		r.setTargetOfferPayoffValue(229.37);
		r.setRevenueSpit(76.46);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.55);
		r.setTargetOfferPayoffValue(233.95);
		r.setRevenueSpit(77.98);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.6);
		r.setTargetOfferPayoffValue(238.54);
		r.setRevenueSpit(79.51);
		r.setAirtimePayoff(70);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.65);
		r.setTargetOfferPayoffValue(243.13);
		r.setRevenueSpit(81.04);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.7);
		r.setTargetOfferPayoffValue(247.71);
		r.setRevenueSpit(82.57);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.75);
		r.setTargetOfferPayoffValue(252.3);
		r.setRevenueSpit(84.1);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.8);
		r.setTargetOfferPayoffValue(256.89);
		r.setRevenueSpit(85.63);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.85);
		r.setTargetOfferPayoffValue(261.48);
		r.setRevenueSpit(87.16);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.9);
		r.setTargetOfferPayoffValue(266.06);
		r.setRevenueSpit(88.69);
		r.setAirtimePayoff(80);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(2.95);
		r.setTargetOfferPayoffValue(270.65);
		r.setRevenueSpit(90.22);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.0);
		r.setTargetOfferPayoffValue(275.24);
		r.setRevenueSpit(91.75);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.05);
		r.setTargetOfferPayoffValue(279.83);
		r.setRevenueSpit(93.28);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.1);
		r.setTargetOfferPayoffValue(284.41);
		r.setRevenueSpit(94.80);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.15);
		r.setTargetOfferPayoffValue(289.00);
		r.setRevenueSpit(96.33);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.20);
		r.setTargetOfferPayoffValue(293.59);
		r.setRevenueSpit(97.86);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.25);
		r.setTargetOfferPayoffValue(298.17);
		r.setRevenueSpit(99.39);
		r.setAirtimePayoff(90);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.3);
		r.setTargetOfferPayoffValue(302.76);
		r.setRevenueSpit(100.92);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.35);
		r.setTargetOfferPayoffValue(307.35);
		r.setRevenueSpit(102.45);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.4);
		r.setTargetOfferPayoffValue(311.94);
		r.setRevenueSpit(103.98);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.45);
		r.setTargetOfferPayoffValue(316.52);
		r.setRevenueSpit(105.51);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.5);
		r.setTargetOfferPayoffValue(321.11);
		r.setRevenueSpit(107.04);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.55);
		r.setTargetOfferPayoffValue(325.70);
		r.setRevenueSpit(108.57);
		r.setAirtimePayoff(100);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.6);
		r.setTargetOfferPayoffValue(330.29);
		r.setRevenueSpit(110.10);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.65);
		r.setTargetOfferPayoffValue(334.87);
		r.setRevenueSpit(111.62);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.7);
		r.setTargetOfferPayoffValue(339.46);
		r.setRevenueSpit(113.15);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.75);
		r.setTargetOfferPayoffValue(344.05);
		r.setRevenueSpit(114.68);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.8);
		r.setTargetOfferPayoffValue(348.63);
		r.setRevenueSpit(116.21);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.85);
		r.setTargetOfferPayoffValue(353.22);
		r.setRevenueSpit(117.74);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.9);
		r.setTargetOfferPayoffValue(357.81);
		r.setRevenueSpit(119.27);
		r.setAirtimePayoff(110);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(3.95);
		r.setTargetOfferPayoffValue(362.4);
		r.setRevenueSpit(120.80);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.0);
		r.setTargetOfferPayoffValue(366.98);
		r.setRevenueSpit(122.33);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.05);
		r.setTargetOfferPayoffValue(371.57);
		r.setRevenueSpit(123.86);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.10);
		r.setTargetOfferPayoffValue(376.16);
		r.setRevenueSpit(125.39);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.15);
		r.setTargetOfferPayoffValue(380.75);
		r.setRevenueSpit(126.92);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.2);
		r.setTargetOfferPayoffValue(385.33);
		r.setRevenueSpit(128.44);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.25);
		r.setTargetOfferPayoffValue(389.92);
		r.setRevenueSpit(129.97);
		r.setAirtimePayoff(120);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.3);
		r.setTargetOfferPayoffValue(394.51);
		r.setRevenueSpit(131.50);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.35);
		r.setTargetOfferPayoffValue(399.1);
		r.setRevenueSpit(133.03);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.4);
		r.setTargetOfferPayoffValue(403.68);
		r.setRevenueSpit(134.56);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.45);
		r.setTargetOfferPayoffValue(408.27);
		r.setRevenueSpit(136.09);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.5);
		r.setTargetOfferPayoffValue(412.86);
		r.setRevenueSpit(137.62);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.55);
		r.setTargetOfferPayoffValue(417.44);
		r.setRevenueSpit(139.15);
		r.setAirtimePayoff(130);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.6);
		r.setTargetOfferPayoffValue(422.03);
		r.setRevenueSpit(140.68);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.65);
		r.setTargetOfferPayoffValue(426.62);
		r.setRevenueSpit(142.21);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.7);
		r.setTargetOfferPayoffValue(431.21);
		r.setRevenueSpit(143.74);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.75);
		r.setTargetOfferPayoffValue(435.79);
		r.setRevenueSpit(145.26);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.8);
		r.setTargetOfferPayoffValue(440.38);
		r.setRevenueSpit(146.79);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.85);
		r.setTargetOfferPayoffValue(444.97);
		r.setRevenueSpit(148.32);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.90);
		r.setTargetOfferPayoffValue(449.56);
		r.setRevenueSpit(149.85);
		r.setAirtimePayoff(140);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(4.95);
		r.setTargetOfferPayoffValue(454.14);
		r.setRevenueSpit(151.38);
		r.setAirtimePayoff(150);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(5.00);
		r.setTargetOfferPayoffValue(458.73);
		r.setRevenueSpit(152.91);
		r.setAirtimePayoff(150);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(6.00);
		r.setTargetOfferPayoffValue(550.48);
		r.setRevenueSpit(183.49);
		r.setAirtimePayoff(180);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(7.00);
		r.setTargetOfferPayoffValue(642.22);
		r.setRevenueSpit(214.07);
		r.setAirtimePayoff(210);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(8.00);
		r.setTargetOfferPayoffValue(733.97);
		r.setRevenueSpit(244.66);
		r.setAirtimePayoff(240);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(9.00);
		r.setTargetOfferPayoffValue(825.71);
		r.setRevenueSpit(275.24);
		r.setAirtimePayoff(270);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(10.00);
		r.setTargetOfferPayoffValue(917.46);
		r.setRevenueSpit(305.82);
		r.setAirtimePayoff(300);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(11.00);
		r.setTargetOfferPayoffValue(1009.21);
		r.setRevenueSpit(336.4);
		r.setAirtimePayoff(330);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(12.00);
		r.setTargetOfferPayoffValue(1100.95);
		r.setRevenueSpit(366.98);
		r.setAirtimePayoff(360);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(13.00);
		r.setTargetOfferPayoffValue(1192.7);
		r.setRevenueSpit(397.57);
		r.setAirtimePayoff(390);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(14.00);
		r.setTargetOfferPayoffValue(1284.44);
		r.setRevenueSpit(428.15);
		r.setAirtimePayoff(420);
		listRows.add(r);

		r = new DenominationModelRow();
		r.setSourceOfferPayoffValue(15.00);
		r.setTargetOfferPayoffValue(1376.19);
		r.setRevenueSpit(458.73);
		r.setAirtimePayoff(450);
		listRows.add(r);

	}

	public double calculateAirtimeReward(double offerPayout) {
		double minDelta = 0;
		int minDeltaIndex = 0;

		//set defaults
		minDelta = Math.abs(listRows.get(0).getSourceOfferPayoffValue()-offerPayout);
		minDeltaIndex = 0;
		//System.out.println("value: "+listRows.get(0).getSourceOfferPayoffValue()+" delta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
		
		for(int i=1;i<listRows.size();i++) {
			DenominationModelRow r = listRows.get(i);
			double delta = Math.abs(r.getSourceOfferPayoffValue()-offerPayout);
			if(delta < minDelta) {
				minDelta = delta;
				minDeltaIndex = i;
			}
			//System.out.println("value: "+r.getSourceOfferPayoffValue()+" delta: "+delta+" minDelta: "+minDelta+" minDeltaIndex: "+minDeltaIndex);
		}
		
		//System.out.println("=> value: "+" delta: "+minDelta+" minDeltaIndex: "+minDeltaIndex+" airtime payout: "+listRows.get(minDeltaIndex).airtimePayoff);
		System.out.println("["+offerPayout+"=>"+listRows.get(minDeltaIndex).getAirtimePayoff()+"]"); //value: "+" delta: "+minDelta+" minDeltaIndex: "+minDeltaIndex+" airtime payout: "+listRows.get(minDeltaIndex).airtimePayoff);

		return listRows.get(minDeltaIndex).getAirtimePayoff();
	}
	
}
