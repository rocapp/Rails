/*
 * Created on 05mar2005
 *
 */
package game;

import java.util.*;

/**
 * @author Erik Vos
 */
public abstract class Company implements CompanyI, ConfigurableComponentI,
		Cloneable
{

	protected static int numberOfCompanies = 0;
	protected String name;
	protected CompanyTypeI type;
	protected int companyNumber; // For internal use
	protected Portfolio portfolio = null;

	private ArrayList tokens = new ArrayList();
	private boolean hasTokens = false;
	protected int numCityTokens = 0;
	protected int maxCityTokens = 0;
	
	/**
	 * The value per certificate at the end of the game. Default 0 (for
	 * privates).
	 */
	protected int value = 0;
	/**
	 * Twice the amount each certificate counts against the limit (this way we
	 * take care for certs that count for 0.5)
	 */
	protected int certLimitCount = 2;

	protected boolean closed = false;

	public Company()
	{
		this.companyNumber = numberOfCompanies++;
	}

	public void init(String name, CompanyTypeI type)
	{
		this.name = name;
		this.type = type;
	}

	/**
	 * @return Number of Companies
	 */
	public static int getNumberOfCompanies()
	{
		return numberOfCompanies;
	}

	/**
	 * @return This company's number
	 */
	public int getNumber()
	{
		return companyNumber;
	}

	/**
	 * @return whether this company is closed
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Close this company.
	 */
	public void setClosed()
	{
		closed = true;
	}

	/**
	 * @return Type of company (Public/Private)
	 */
	public CompanyTypeI getType()
	{
		return type;
	}

	/**
	 * @return String for type of company (Public/Private)
	 */
	public String getTypeName()
	{
		return type.getName();
	}

	/**
	 * @return Name of company
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public int getCertLimitCount()
	{
		return certLimitCount;
	}

	/**
	 * @return This company's number
	 */
	public int getCompanyNumber()
	{
		return companyNumber;
	}

	/**
	 * @return Value of this company
	 */
	public int getValue()
	{
		return value;
	}

	/**
	 * @param i
	 */
	public void setCertLimitCount(int i)
	{
		certLimitCount = i;
	}

	/**
	 * @param i
	 */
	public void setValue(int i)
	{
		value = i;
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public void addToken(CompanyI company)
	{
		tokens.add(company);
		hasTokens = true;
	}
	
	public void playToken(MapHex hex)
	{
		tokens.add(hex);
		hasTokens = true;
		numCityTokens--;
	}
	
	public List getTokens()
	{
		return tokens;
	}

	public boolean hasTokens()
	{
		return hasTokens;
	}

	public boolean removeToken(CompanyI company)
	{
		int index = tokens.indexOf(company);
		if (index >= 0 && tokens.get(index) instanceof Company)
		{
			tokens.remove(index);

			if (tokens.size() < 1)
			{
				hasTokens = false;
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	
	public int getNumCityTokens()
	{
		return numCityTokens;
	}

	
	public void setNumCityTokens(int numCityTokens)
	{
		this.numCityTokens = numCityTokens;
	}
	
	public int getMaxCityTokens()
	{
		return maxCityTokens;
	}

}
