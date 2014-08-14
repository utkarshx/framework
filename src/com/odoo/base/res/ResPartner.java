/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */

package com.odoo.base.res;

import android.content.Context;

import com.odoo.addons.partners.model.ResPartnerCategory;
import com.odoo.addons.partners.model.ResPartnerTitle;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.util.ODate;

/**
 * The Class Res_PartnerDBHelper.
 */
public class ResPartner extends OModel {

	OColumn name = new OColumn("Name", OText.class);
	OColumn is_company = new OColumn("Is Company", OBoolean.class)
			.setDefault(false);
	OColumn image_small = new OColumn("Image", OBlob.class).setDefault(false);
	OColumn street = new OColumn("Street", OText.class);
	OColumn street2 = new OColumn("Street2", OText.class);
	OColumn city = new OColumn("City", OText.class);
	OColumn zip = new OColumn("Zip", OVarchar.class, 10);
	OColumn website = new OColumn("Website", OText.class);
	OColumn phone = new OColumn("Phone", OText.class);
	OColumn mobile = new OColumn("Mobile", OText.class);
	OColumn email = new OColumn("Email", OText.class);
	OColumn company_id = new OColumn("Company", ResCompany.class,
			RelationType.ManyToOne).addDomain("is_company", "=", true);

	// Extra Demo Module Columns
	OColumn date = new OColumn("Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn title = new OColumn("Title", ResPartnerTitle.class,
			RelationType.ManyToOne);
	OColumn parent_id = new OColumn("Related Company", ResPartner.class,
			RelationType.ManyToOne).addDomain("is_company", "=", true);
	OColumn child_ids = new OColumn("Contacts", ResPartner.class,
			RelationType.OneToMany).setRelatedColumn("parent_id");
	OColumn comment = new OColumn("Notes", OText.class);
	OColumn category_id = new OColumn("Tags", ResPartnerCategory.class,
			RelationType.ManyToMany);
	OColumn customer = new OColumn("Customer", OBoolean.class);
	OColumn supplier = new OColumn("Supplier", OBoolean.class);
	OColumn state_id = new OColumn("State", ResCountry.class,
			RelationType.ManyToOne);
	OColumn country_id = new OColumn("Country", ResCountry.class,
			RelationType.ManyToOne);

	public ResPartner(Context context) {
		super(context, "res.partner");
	}

}
