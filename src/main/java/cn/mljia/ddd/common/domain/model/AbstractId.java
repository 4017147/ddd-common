//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package cn.mljia.ddd.common.domain.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import cn.mljia.ddd.common.AssertionConcern;

@MappedSuperclass
public abstract class AbstractId extends AssertionConcern implements Identity, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "ID", unique = true, nullable = false, length = 32)
	private String id;

	public String id() {
		return this.id;
	}

	@Override
	public boolean equals(Object anObject) {
		boolean equalObjects = false;

		if (anObject != null && this.getClass() == anObject.getClass()) {
			AbstractId typedObject = (AbstractId) anObject;
			equalObjects = this.id().equals(typedObject.id());
		}

		return equalObjects;
	}

	@Override
	public int hashCode() {
		int hashCodeValue = +(this.hashOddValue() * this.hashPrimeValue()) + this.id().hashCode();

		return hashCodeValue;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [id=" + id + "]";
	}

	protected AbstractId(String anId) {
		this();

		this.setId(anId);
	}

	protected AbstractId() {
		super();
	}

	protected abstract int hashOddValue();

	protected abstract int hashPrimeValue();

	protected void validateId(String anId) {
		// implemented by subclasses for validation.
		// throws a runtime exception if invalid.
	}

	private void setId(String anId) {
		this.assertArgumentNotEmpty(anId, "The basic identity is required.");
		this.assertArgumentLength(anId, 36, "The basic identity must be 36 characters.");

		this.validateId(anId);

		this.id = anId;
	}
}
