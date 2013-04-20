package compecon.engine.dao;

import java.io.Serializable;
import java.util.List;

public interface IGenericDAO<T, ID extends Serializable> {

	public T find(ID id);

	public T findRandom();

	public List<T> findAll();

	public void save(T entity);

	public void merge(T entity);

	public void delete(T entity);
}