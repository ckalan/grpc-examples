package com.cagataykalan.grpc.client;

import com.cagataykalan.grpc.lib.CountryServiceGrpc.CountryServiceBlockingStub;
import com.cagataykalan.grpc.lib.QueryCountryRequest;
import java.util.Scanner;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcClientApplication implements CommandLineRunner {

    private static final int DEFAULT_PAGE_SIZE = 2;

    // Autowired
    @GrpcClient("example-grpc-server")
    private CountryServiceBlockingStub countryServiceBlockingStub;

    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        SpringApplication.run(GrpcClientApplication.class, args);
        scanner.close();
    }

    @Override
    public void run(String... args) {
        System.out.println("Enter county name prefix");
        var prefix = scanner.nextLine();

        var pageSize = readPageSize();
        var query = QueryCountryRequest.newBuilder()
                .setPageSize(pageSize)
                .setPrefix(prefix)
                .build();

        var resultStream = countryServiceBlockingStub.query(query);
        resultStream.forEachRemaining(countrySearchResultPage -> {
            countrySearchResultPage.getItemsList().forEach(item -> {
                System.out.printf("%s(%s)%n", item.getName(), item.getCode());
            });
            scanner.nextLine();
        });

        System.out.println("No more results");
    }

    private int readPageSize() {
        System.out.printf("Enter page size (default %d)%n", DEFAULT_PAGE_SIZE);

        var pageSize = scanner.nextLine();
        if (pageSize.trim().length() == 0) {
            return 2;
        }

        return Integer.parseInt(pageSize);
    }
}
