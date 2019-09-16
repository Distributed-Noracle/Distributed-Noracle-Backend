package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class NoracleAgentProfile implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NoracleAgentProfile))
			return false;

		final NoracleAgentProfile other = (NoracleAgentProfile) obj;
		return StringUtils.equals(name, other.name);
	}
}
