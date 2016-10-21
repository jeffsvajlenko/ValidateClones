
public class Clone {
	private int toolid;
	private int cloneid;
	private String fragment1;
	private String fragment2;
	private String text1;
	private String text2;
	private Boolean validation = null;
	
	public boolean getValidation() {
		return validation;
	}
	
	public void setValidation(boolean validation) {
		this.validation = validation;
	}
	
	public Clone(int toolid, int cloneid, String fragment1, String fragment2, String text1, String text2) {
		super();
		this.toolid = toolid;
		this.cloneid = cloneid;
		this.fragment1 = fragment1;
		this.fragment2 = fragment2;
		this.text1 = text1;
		this.text2 = text2;
	}

	public int getToolID() {
		return toolid;
	}
	
	public String getFragment1() {
		return fragment1;
	}
	
	public String getFragment2() {
		return fragment2;
	}
	
	public String getText1() {
		return text1;
	}
	
	public String getText2() {
		return text2;
	}
	
	public int getCloneId() {
		return this.cloneid;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cloneid;
		result = prime * result + ((fragment1 == null) ? 0 : fragment1.hashCode());
		result = prime * result + ((fragment2 == null) ? 0 : fragment2.hashCode());
		result = prime * result + toolid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clone other = (Clone) obj;
		if (cloneid != other.cloneid)
			return false;
		if (fragment1 == null) {
			if (other.fragment1 != null)
				return false;
		} else if (!fragment1.equals(other.fragment1))
			return false;
		if (fragment2 == null) {
			if (other.fragment2 != null)
				return false;
		} else if (!fragment2.equals(other.fragment2))
			return false;
		if (toolid != other.toolid)
			return false;
		return true;
	}

	public String resultString(boolean tp) {
		if(tp) {
			return toolid + "," + cloneid + "," + "true" + "," + fragment1 + "," + fragment2;
		} else {
			return toolid + "," + cloneid + "," + "false" + "," + fragment1 + "," + fragment2;
		}
	}
	
}