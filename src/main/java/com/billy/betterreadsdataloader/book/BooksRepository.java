package com.billy.betterreadsdataloader.book;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BooksRepository extends CassandraRepository<Books, String> {
}
