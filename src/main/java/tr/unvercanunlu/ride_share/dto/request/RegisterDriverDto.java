package tr.unvercanunlu.ride_share.dto.request;

public record RegisterDriverDto(
    String name,
    String email,
    String phone,
    String plate
) {

}
