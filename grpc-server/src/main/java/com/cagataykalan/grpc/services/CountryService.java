package com.cagataykalan.grpc.services;

import com.cagataykalan.grpc.lib.CountrySearchResultPage;
import com.cagataykalan.grpc.lib.CountrySearchResultPage.Item;
import com.cagataykalan.grpc.lib.CountryServiceGrpc;
import com.cagataykalan.grpc.lib.QueryCountryRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Data;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

@GrpcService
public class CountryService extends CountryServiceGrpc.CountryServiceImplBase {

    @Value("classpath:countries.json")
    private Resource countriesResource;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void query(
            QueryCountryRequest request, StreamObserver<CountrySearchResultPage> responseObserver) {
        try {
            getCountryResultPages(request).map(this::mapCountryListToResponsePage)
                    .forEach(responseObserver::onNext);
        } catch (Throwable e) {
            responseObserver.onError(e);
        }

        responseObserver.onCompleted();
    }

    private CountrySearchResultPage mapCountryListToResponsePage(List<Country> page) {
        return CountrySearchResultPage.newBuilder()
                .addAllItems(page.stream()
                        .map(this::mapCountryToResultItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private Item mapCountryToResultItem(Country country) {
        return Item.newBuilder().setName(country.getName()).setCode(country.getCode()).build();
    }

    private Stream<List<Country>> getCountryResultPages(
            QueryCountryRequest request) throws IOException {
        try (var stream = countriesResource.getInputStream()) {
            var allCountries = MAPPER.readValue(stream, Country[].class);
            var matchingCountries = Arrays.stream(allCountries)
                    .filter(country -> country.getName()
                            .toLowerCase(Locale.ROOT)
                            .startsWith(request.getPrefix().toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
            return chunk(matchingCountries, request.getPageSize());
        }
    }

    public static <T> Stream<List<T>> chunk(List<T> source, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize = " + chunkSize);
        }
        int size = source.size();
        if (size <= 0) {
            return Stream.empty();
        }
        int fullChunks = (size - 1) / chunkSize;
        return IntStream.range(0, fullChunks + 1)
                .mapToObj(n -> source.subList(n * chunkSize,
                        n == fullChunks ? size : (n + 1) * chunkSize));
    }

    @Data
    private static class Country {

        @JsonProperty("Name")
        private String name;
        @JsonProperty("Code")
        private String code;
    }
}
