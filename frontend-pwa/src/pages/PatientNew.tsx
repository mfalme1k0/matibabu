import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { patientsApi } from "@/lib/patients";
import { Button, Card, ErrorText, Field, Input, Select } from "@/components/ui";

// Mirrors RegisterPatientRequest validation on the backend.
const schema = z.object({
  firstName: z.string().trim().min(1, "Required"),
  lastName: z.string().trim().min(1, "Required"),
  dateOfBirth: z
    .string()
    .min(1, "Required")
    .refine((d) => new Date(d) <= new Date(), "Cannot be in the future"),
  gender: z.enum(["MALE", "FEMALE", "OTHER", "UNKNOWN"]),
  phoneNumber: z.string().regex(/^\+?[0-9]{7,15}$/, "7-15 digits, optional + prefix"),
  address: z.string().trim().min(1, "Required"),
  nationalId: z.string().max(50).optional(),
  birthCertificateNumber: z.string().max(50).optional(),
});
type Form = z.infer<typeof schema>;

export default function PatientNew() {
  const nav = useNavigate();
  const qc = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>({ resolver: zodResolver(schema), defaultValues: { gender: "UNKNOWN" } });

  const create = useMutation({
    mutationFn: (v: Form) =>
      patientsApi.register({
        ...v,
        nationalId: v.nationalId || undefined,
        birthCertificateNumber: v.birthCertificateNumber || undefined,
      }),
    onSuccess: (p) => {
      qc.invalidateQueries({ queryKey: ["patients"] });
      nav(`/patients/${p.id}`);
    },
  });

  return (
    <Card className="mx-auto max-w-xl">
      <h1 className="mb-4 text-2xl font-bold">Register patient</h1>
      <form onSubmit={handleSubmit((v) => create.mutate(v))} className="grid gap-4 sm:grid-cols-2">
        <Field label="First name" error={errors.firstName?.message}>
          <Input {...register("firstName")} />
        </Field>
        <Field label="Last name" error={errors.lastName?.message}>
          <Input {...register("lastName")} />
        </Field>
        <Field label="Date of birth" error={errors.dateOfBirth?.message}>
          <Input type="date" {...register("dateOfBirth")} />
        </Field>
        <Field label="Gender" error={errors.gender?.message}>
          <Select {...register("gender")}>
            <option value="FEMALE">Female</option>
            <option value="MALE">Male</option>
            <option value="OTHER">Other</option>
            <option value="UNKNOWN">Unknown</option>
          </Select>
        </Field>
        <Field label="Phone number" error={errors.phoneNumber?.message}>
          <Input inputMode="tel" placeholder="+254712345678" {...register("phoneNumber")} />
        </Field>
        <Field label="Address" error={errors.address?.message}>
          <Input {...register("address")} />
        </Field>
        <Field label="National ID (optional)" error={errors.nationalId?.message}>
          <Input {...register("nationalId")} />
        </Field>
        <Field label="Birth certificate no. (optional)" error={errors.birthCertificateNumber?.message}>
          <Input {...register("birthCertificateNumber")} />
        </Field>
        {create.isError && (
          <div className="sm:col-span-2">
            <ErrorText>{(create.error as Error).message}</ErrorText>
          </div>
        )}
        <div className="flex gap-2 sm:col-span-2">
          <Button type="submit" disabled={create.isPending}>
            {create.isPending ? "Saving…" : "Register"}
          </Button>
          <Button type="button" variant="ghost" onClick={() => nav(-1)}>
            Cancel
          </Button>
        </div>
      </form>
    </Card>
  );
}
