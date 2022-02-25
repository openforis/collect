package org.openforis.collect.persistence.liquibase;

public enum LiquibaseSupportedDBMS {

	H2("H2", "h2"),
	POSTGRESQL("PostgreSQL", "postgresql"),
	SQLITE("SQLite", "sqlite"),
	SQLITE_ANDROID("SQLite for Android", "sqlite");

	private String productName;
	private String liquibaseDbms;

	LiquibaseSupportedDBMS(String productName, String liquibaseDbms) {
		this.productName = productName;
		this.liquibaseDbms = liquibaseDbms;
	}

	public String getProductName() {
		return productName;
	}

	public String getLiquibaseDbms() {
		return liquibaseDbms;
	}

	public static LiquibaseSupportedDBMS findByProductName(String productName) {
		for (LiquibaseSupportedDBMS db : values()) {
			if (db.productName.equalsIgnoreCase(productName))
				return db;
		}
		return null;
	}

}