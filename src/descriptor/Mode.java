/*
 * This file is part of the PDF Descriptor project.
 * Copyright (C) 2021 Pogromca SCP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package descriptor;

import java.util.regex.Pattern;
import org.json.easy.dom.JSONObject;

/**
 * Represents a work mode
 */
public class Mode
{
	/**
	 * Mode name to display
	 */
	public final String name;
	
	/**
	 * Mode pattern
	 */
	public final Pattern pattern;
	
	/**
	 * Mode page number
	 */
	public final int page;
	
	/**
	 * Set to true to add parenthesis
	 */
	public final boolean addParenthesis;
	
	/**
	 * Set to true to revert the text
	 */
	public final boolean revertText;
	
	/**
	 * Mode postfix
	 */
	public final String postfix;
	
	/**
	 * Creates new mode
	 * 
	 * @param obj Config object to use
	 */
	public Mode(final JSONObject obj)
	{	
		name = obj == null ? null : obj.getStringField("name");
		pattern = obj == null ? null : Pattern.compile(obj.getStringField("pattern"));
		page = obj == null ? 1 : (int) obj.getNumberField("page");
		addParenthesis = obj == null ? false : obj.getBooleanField("parenthesis");
		revertText = obj == null ? true : obj.getBooleanField("revert");
		postfix = obj == null ? null : obj.getStringField("postfix");
	}
	
	/**
	 * Converts this object into a human readable string
	 * 
	 * @return Human readable string
	 */
	@Override
	public String toString()
	{
		return name == null ? "Tryb debugowania (drukowanie)" : name;
	}
}
