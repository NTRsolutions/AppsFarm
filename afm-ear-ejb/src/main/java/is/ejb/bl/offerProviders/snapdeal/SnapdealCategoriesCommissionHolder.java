package is.ejb.bl.offerProviders.snapdeal;

import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.RealmEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class SnapdealCategoriesCommissionHolder {

	@Inject
	private DAORealm daoRealm;
	
	private List<SnapdealCategoryCommision> categoriesCommision;

	@PostConstruct
	public void init() {
		categoriesCommision = new ArrayList<SnapdealCategoryCommision>();

		SnapdealCategoryCommision mobilesAndTablets = new SnapdealCategoryCommision();
		mobilesAndTablets.setCommision(2.5);
		mobilesAndTablets.setSuperCategory("Mobiles and Tablets");
		String[] mobilesAndTabletsArray = { "Mobiles & Tablets" };
		mobilesAndTablets.setCategories(Arrays.asList(mobilesAndTabletsArray));
		categoriesCommision.add(mobilesAndTablets);

		SnapdealCategoryCommision electronics1 = new SnapdealCategoryCommision();
		electronics1.setCommision(2.5);
		electronics1.setSuperCategory("Electronics-1");
		String[] electronicsArray1 = { "Computers & Peripherals", "Cameras & Accessories", "Gaming",
				"TVs, Audio & Video", "Appliances" };
		electronics1.setCategories(Arrays.asList(electronicsArray1));
		categoriesCommision.add(electronics1);

		SnapdealCategoryCommision electronics2 = new SnapdealCategoryCommision();
		electronics2.setCommision(6.0);
		electronics2.setSuperCategory("Electronics-2");
		String[] electronicsArray2 = { "Office Equipment" };
		electronics2.setCategories(Arrays.asList(electronicsArray2));
		categoriesCommision.add(electronics2);

		SnapdealCategoryCommision electronics3 = new SnapdealCategoryCommision();
		electronics3.setCommision(10.0);
		electronics3.setSuperCategory("Electronics-3");
		String[] electronicsArray3 = { "Movies & Music" };
		electronics3.setCategories(Arrays.asList(electronicsArray3));
		categoriesCommision.add(electronics3);

		SnapdealCategoryCommision mensFashion = new SnapdealCategoryCommision();
		mensFashion.setCommision(8.0);
		mensFashion.setSuperCategory("Men's Fashion");
		String[] mensFashionArray = { "Men's Footwear", "Men's Clothing", "Watches", "Fashion Accessories",
				"Fragrances" };
		mensFashion.setCategories(Arrays.asList(mensFashionArray));
		categoriesCommision.add(mensFashion);

		SnapdealCategoryCommision womensFashion = new SnapdealCategoryCommision();
		womensFashion.setCommision(8.0);
		womensFashion.setSuperCategory("Women's Fashion");
		String[] womensFashionArray = { "Women's Clothing", "Women's Ethnic Wear", "Women's Footwear", "Watches",
				"The Designer Studio", "Fashion Jewellery", "Fragrances", "Fashion Accessories", "Eyewear",
				"Handbags & Clutches", "Maternity", "Women's Accessories", "Fusion Wear", };
		womensFashion.setCategories(Arrays.asList(womensFashionArray));
		categoriesCommision.add(womensFashion);

		SnapdealCategoryCommision kidsFashion = new SnapdealCategoryCommision();
		kidsFashion.setCommision(8.0);
		kidsFashion.setSuperCategory("Kid's Fashion");
		String[] kidsFashionArray = { "Baby Care", "Boys Clothing (2-8 yrs)", "Boy' Clothing (8-14 yrs)",
				"Girls Clothing (2-8 yrs)", "Girls Clothing (8-14 yrs)", "Infant Wear", "Kids Footwear",
				"Kids Apparel and Accessories", "Stationery", "Toys & Games","kids Eyewear" };
		kidsFashion.setCategories(Arrays.asList(kidsFashionArray));
		categoriesCommision.add(kidsFashion);

		SnapdealCategoryCommision motors = new SnapdealCategoryCommision();
		motors.setCommision(6.0);
		motors.setSuperCategory("Motors");
		String[] motorsArray = { "Motors" };
		motors.setCategories(Arrays.asList(motorsArray));
		categoriesCommision.add(motors);

		SnapdealCategoryCommision fmcg = new SnapdealCategoryCommision();
		fmcg.setCommision(8.0);
		fmcg.setSuperCategory("FMCG");
		String[] fmcgArray = { "Beauty & Personal Care", "World Food / Indian Food", "Gourmet",
				"Health, Wellness & Medicine", "Nutrition and Supplements", "Household Essentials" };
		fmcg.setCategories(Arrays.asList(fmcgArray));
		categoriesCommision.add(fmcg);

		SnapdealCategoryCommision gm = new SnapdealCategoryCommision();
		gm.setCommision(6.0);
		gm.setSuperCategory("GM");
		String[] gmArray = { "Bags & Luggage", "Furniture", "Tools and Hardware", "Home Decoratives",
				"Home Improvement", "Kitchenware", "Home Services", "Chocolates and Snacks", "Gifting & Events",
				"Fitness", "Hobbies", "Musical Instruments", "Sports & Fitness", "Agriculture",
				"Kitchen Appliances" };
		gm.setCategories(Arrays.asList(gmArray));
		categoriesCommision.add(gm);

		SnapdealCategoryCommision books = new SnapdealCategoryCommision();
		books.setCommision(3.0);
		books.setSuperCategory("Books");
		String[] booksArray = { "Books" };
		books.setCategories(Arrays.asList(booksArray));
		categoriesCommision.add(books);

		SnapdealCategoryCommision digitalProducts1 = new SnapdealCategoryCommision();
		digitalProducts1.setCommision(10.0);
		digitalProducts1.setSuperCategory("Digital Products-1");
		String[] digitalProducts1Array = { "Movies & Music" };
		digitalProducts1.setCategories(Arrays.asList(digitalProducts1Array));
		categoriesCommision.add(digitalProducts1);

		SnapdealCategoryCommision digitalProducts2 = new SnapdealCategoryCommision();
		digitalProducts2.setCommision(10.0);
		digitalProducts2.setSuperCategory("Digital Products-2");
		String[] digitalProducts2Array = { "Online Education" };
		digitalProducts2.setCategories(Arrays.asList(digitalProducts2Array));
		categoriesCommision.add(digitalProducts2);
		
		RealmEntity realm = getRealm();
		double commisionPercentage = 0.5;
		if (realm != null){
			commisionPercentage = realm.getSnapdealPercentageCommision();
		}
		for (SnapdealCategoryCommision commision : categoriesCommision){
			commision.setCommision(commision.getCommision()*commisionPercentage);
		}

	}
	public RealmEntity getRealm(){
		try{
			return daoRealm.findByName("BPM");
		}catch (Exception exception){
			exception.printStackTrace();
			return null;
		}
	}

	public List<SnapdealCategoryCommision> getCategoriesCommision() {
		return categoriesCommision;
	}

	public void setCategoriesCommision(List<SnapdealCategoryCommision> categoriesCommision) {
		this.categoriesCommision = categoriesCommision;
	}

	

}
