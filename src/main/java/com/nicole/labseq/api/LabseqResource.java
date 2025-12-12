package com.nicole.labseq.api;

import com.nicole.labseq.api.dto.LabseqResponse;
import com.nicole.labseq.domain.LabseqService;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.math.BigInteger;

@Path("/labseq")
@Produces(MediaType.APPLICATION_JSON)
public class LabseqResource {

    @Inject
    LabseqService service;

    @GET
    @Path("/{n}")
    @Operation(
        summary = "Obtém l(n) da sequência labseq",
        description = "l(0)=0, l(1)=1, l(2)=0, l(3)=1; para n>3, l(n)=l(n-4)+l(n-3)."
    )
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Índice inválido")
    @CacheResult(cacheName = "labseq-endpoint")
    public Response getValue(
        @Parameter(description = "Índice n (inteiro não-negativo)", example = "100000", required = true)
        @PathParam("n") long n
    ) {
        if (n < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Invalid index: must be non-negative."))
                    .build();
        }
        BigInteger val = service.value(n);
        return Response.ok(new LabseqResponse(n, val.toString())).build();
    }

    public static class ErrorMessage {
        public String message;
        public ErrorMessage() {}
        public ErrorMessage(String m) { this.message = m; }
    }
}
